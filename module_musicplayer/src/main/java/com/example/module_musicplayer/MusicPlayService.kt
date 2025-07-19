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
import com.example.lib.base.Song
import data.ListMusicData

class MusicPlayService : Service() {
    private val CHANNEL_ID = "music_service_channel"
    private val NOTIFICATION_ID = 10086
    val currentSongLiveData = MutableLiveData<Song?>()
    val isPlayingLiveData = MutableLiveData<Boolean>()

    private var mediaPlayer: MediaPlayer? = null
    var currentUrl: String? = null
    var currentSong: Song? = null
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

                // 核心修复：系统播放播放完成监听，触发自定义回调
                setOnCompletionListener {
                    this@MusicPlayService.isPlaying = false
                    onPlayStateChanged?.invoke(false, it.duration)
                    // 调用Activity设置的播放完成回调（自动播放下一首）
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

    // 播放新歌曲或继续播放当前歌曲
    fun play(url: String, song: Song?) {
        currentUrl = url
        currentSong = song
        isPlaying=true
        currentSongLiveData.postValue(song)
        Log.d("ServiceData", "发送歌曲更新: ${song?.name}")
        isPlayingLiveData.postValue(true)
        if (url.isBlank()) {
            Log.e("ServiceError", "播放地址为空")
            onPlayStateChanged?.invoke(false, 0)
            return
        }

        try {
            // 关键修复：即使是同一URL，也强制重置并重新加载（确保单曲循环能从头播放）
            mediaPlayer?.reset() // 无论是否同一首歌，先重置
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

    // 暂停播放
    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            onPlayStateChanged?.invoke(false, mediaPlayer!!.duration)
            isPlayingLiveData.postValue(false)
        }
    }

    // 停止播放并重置
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset() // 完全重置，确保下一首从头开始
        }
        isPlaying = false
        onPlayStateChanged?.invoke(false, 0)
    }

    // 从暂停状态恢复播放
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在播放")
            .setContentText(currentUrl?.split("/")?.last() ?: "未知歌曲")
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
    }
}
