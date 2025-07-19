package com.example.module_musicplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import data.ListMusicData

class MusicPlayService : Service() {
    private val CHANNEL_ID = "music_service_channel"
    private val NOTIFICATION_ID = 10086
    // 修复1：统一使用data包下的Song类型
    val currentSongLiveData = MutableLiveData<ListMusicData.Song?>()
    val isPlayingLiveData = MutableLiveData<Boolean>()

    private var mediaPlayer: MediaPlayer? = null
    var currentUrl: String? = null
    // 修复2：将currentSong类型改为ListMusicData.Song
    var currentSong: ListMusicData.Song? = null
    var isPlaying = false // 公开变量便于Activity直接访问

    // 播放完成回调（用于自动播放下一首）
    private var onCompletionListener: (() -> Unit)? = null
    fun setOnCompletionListener(listener: (() -> Unit)?) {
        onCompletionListener = listener
    }

    inner class MusicBinder : Binder() {
        val service: MusicPlayService
            get() = this@MusicPlayService
    }
    private val binder = MusicBinder()

    private var onPlayStateChanged: ((isPlaying: Boolean, duration: Int) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setOnCompletionListener {
                    this@MusicPlayService.isPlaying = false
                    onPlayStateChanged?.invoke(false, it.duration)
                    onCompletionListener?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "播放错误: $what, $extra")
                    this@MusicPlayService.isPlaying = false
                    onPlayStateChanged?.invoke(false, 0)
                    true
                }
            }
        }
    }

    // 修复3：参数类型明确为ListMusicData.Song
    fun play(url: String, song: ListMusicData.Song?) {
        currentUrl = url
        currentSong = song
        isPlaying = true
        // 修复4：LiveData发送正确类型
        currentSongLiveData.postValue(song)
        Log.d("ServiceData", "发送歌曲更新: ${song?.name}")
        isPlayingLiveData.postValue(true)

        if (url.isBlank()) {
            Log.e("ServiceError", "播放地址为空")
            onPlayStateChanged?.invoke(false, 0)
            return
        }

        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    currentUrl = url
                    isPlaying = true
                    onPlayStateChanged?.invoke(true, mp.duration)
                    startForegroundWithNotification()
                } catch (e: Exception) {
                    Log.e("ServicePlay", "播放失败: ${e.message}")
                    isPlaying = false
                    onPlayStateChanged?.invoke(false, 0)
                }
            }
        } catch (e: Exception) {
            Log.e("ServiceError", "播放初始化失败: ${e.message}")
            isPlaying = false
            onPlayStateChanged?.invoke(false, 0)
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            onPlayStateChanged?.invoke(false, mediaPlayer!!.duration)
            isPlayingLiveData.postValue(false)
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
        }
        isPlaying = false
        onPlayStateChanged?.invoke(false, 0)
    }

    fun resume() {
        if (currentUrl != null && !isPlaying) {
            try {
                mediaPlayer?.start()
                isPlaying = true
                onPlayStateChanged?.invoke(true, mediaPlayer?.duration ?: 0)
                startForegroundWithNotification()
            } catch (e: Exception) {
                Log.e("ServiceResume", "恢复播放失败: ${e.message}")
                isPlaying = false
                onPlayStateChanged?.invoke(false, 0)
            }
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun setOnPlayStateChanged(listener: ((isPlaying: Boolean, duration: Int) -> Unit)?) {
        this.onPlayStateChanged = listener
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MusicPlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 修复5：使用正确的歌曲名称显示在通知中
        val songName = currentSong?.name ?: "未知歌曲"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在播放")
            .setContentText(songName) // 显示实际歌曲名而非URL
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification() {
        try {
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            Log.e("ServicePerm", "前台服务权限错误: ${e.message}")
            Toast.makeText(this, "播放服务启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "音乐播放服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于后台播放音乐"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        currentUrl = null
        currentSong = null // 清理资源
    }
}
