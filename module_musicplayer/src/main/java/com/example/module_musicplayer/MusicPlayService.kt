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
    val currentSongLiveData = MutableLiveData<ListMusicData.Song?>()
    val isPlayingLiveData = MutableLiveData<Boolean>()

    private var mediaPlayer: MediaPlayer? = null
    var currentUrl: String? = null
    var currentSong: ListMusicData.Song? = null
    var isPlaying = false

    // 保存当前歌曲的播放进度
    var lastSavedProgress = 0

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
                    lastSavedProgress = 0 // 播放完成重置进度
                    this@MusicPlayService.isPlaying = false
                    onPlayStateChanged?.invoke(false, it.duration)
                    isPlayingLiveData.postValue(false)
                    onCompletionListener?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "播放错误: $what, $extra")
                    this@MusicPlayService.isPlaying = false
                    onPlayStateChanged?.invoke(false, 0)
                    isPlayingLiveData.postValue(false)
                    true
                }
            }
        }
    }

    // 核心修改：新增歌曲ID判断，不同歌曲则重置进度
    fun play(url: String, song: ListMusicData.Song?) {
        // 判断是否切换了歌曲
        val isNewSong = song?.id != currentSong?.id

        // 切换歌曲时强制重置进度
        if (isNewSong) {
            lastSavedProgress = 0
            Log.d("SongChange", "切换到新歌曲 ${song?.name}，重置进度为0")
        }

        currentUrl = url
        currentSong = song
        isPlaying = true
        currentSongLiveData.postValue(song)
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
                    // 只有同一首歌才恢复进度，新歌曲从0开始
                    if (!isNewSong && lastSavedProgress > 0 && lastSavedProgress < mp.duration) {
                        mp.seekTo(lastSavedProgress)
                    } else {
                        mp.seekTo(0) // 新歌曲强制从0开始
                    }
                    mp.start()
                    onPlayStateChanged?.invoke(true, mp.duration)
                    startForegroundWithNotification()
                } catch (e: Exception) {
                    Log.e("ServicePlay", "播放失败: ${e.message}")
                    isPlaying = false
                    onPlayStateChanged?.invoke(false, 0)
                    isPlayingLiveData.postValue(false)
                }
            }
        } catch (e: Exception) {
            Log.e("ServiceError", "播放初始化失败: ${e.message}")
            isPlaying = false
            onPlayStateChanged?.invoke(false, 0)
            isPlayingLiveData.postValue(false)
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            lastSavedProgress = mediaPlayer?.currentPosition ?: 0
            mediaPlayer?.pause()
            isPlaying = false
            onPlayStateChanged?.invoke(false, mediaPlayer!!.duration)
            isPlayingLiveData.postValue(false)
            updateNotification()
        }
    }

    fun stop() {
        lastSavedProgress = 0
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
        }
        isPlaying = false
        onPlayStateChanged?.invoke(false, 0)
        isPlayingLiveData.postValue(false)
    }

    fun resume() {
        if (currentUrl != null && !isPlaying) {
            try {
                // 恢复播放时使用当前歌曲的进度
                if (lastSavedProgress > 0) {
                    mediaPlayer?.seekTo(lastSavedProgress)
                }
                mediaPlayer?.start()
                isPlaying = true
                onPlayStateChanged?.invoke(true, mediaPlayer?.duration ?: 0)
                isPlayingLiveData.postValue(true)
                startForegroundWithNotification()
            } catch (e: Exception) {
                Log.e("ServiceResume", "恢复播放失败: ${e.message}")
                isPlaying = false
                onPlayStateChanged?.invoke(false, 0)
                isPlayingLiveData.postValue(false)
            }
        }
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: lastSavedProgress
    }

    fun seekTo(position: Int) {
        lastSavedProgress = position
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun setOnPlayStateChanged(listener: ((isPlaying: Boolean, duration: Int) -> Unit)?) {
        this.onPlayStateChanged = listener
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MusicPlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("currentProgress", lastSavedProgress)
            putExtra("songId", currentSong?.id?.toString() ?: "")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val songName = currentSong?.name ?: "未知歌曲"
        val artistName = currentSong?.ar?.joinToString { it.name } ?: "未知艺术家"
        val playStateText = if (isPlaying) "正在播放" else "已暂停"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText("$playStateText · $artistName")
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
        lastSavedProgress = mediaPlayer?.currentPosition ?: lastSavedProgress
        mediaPlayer?.release()
        mediaPlayer = null
        currentUrl = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val savedProgress = it.getIntExtra("restoreProgress", -1)
            val songId = it.getStringExtra("restoreSongId")
            if (savedProgress != -1 && songId != null && currentSong?.id.toString() == songId) {
                lastSavedProgress = savedProgress
            }
        }
        return START_STICKY
    }
}
