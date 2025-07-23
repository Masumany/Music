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
    val currentProgress = MutableLiveData<Int>()

    private var mediaPlayer: MediaPlayer? = null
    var currentUrl: String? = null
    var currentSong: ListMusicData.Song? = null
    var isPlaying = false

    // 关键变量：保存当前歌曲的播放进度（无论播放状态）
    var lastSavedProgress = 0

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
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                // 播放中实时更新进度
                val currentPos = mediaPlayer?.currentPosition ?: lastSavedProgress
                lastSavedProgress = currentPos
                currentProgress.postValue(currentPos)
            }
            handler.postDelayed(this, 500)
        }
    }
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
        handler.post(progressUpdateRunnable)
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

    // 播放歌曲（保留进度逻辑）
    fun play(url: String, song: ListMusicData.Song?) {
        val isNewSong = song?.id != currentSong?.id

        // 切换歌曲时才重置进度
        if (isNewSong) {
            lastSavedProgress = 0
            Log.d("ProgressSave", "切换新歌曲 ${song?.name}，重置进度为0")
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
                    // 同一首歌恢复进度，新歌曲从0开始
                    if (!isNewSong && lastSavedProgress > 0 && lastSavedProgress < mp.duration) {
                        mp.seekTo(lastSavedProgress)
                        Log.d("ProgressSave", "恢复进度: $lastSavedProgress")
                    } else {
                        mp.seekTo(0)
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

    // 暂停时保存当前进度
    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            lastSavedProgress = mediaPlayer?.currentPosition ?: 0
            Log.d("ProgressSave", "暂停时保存进度: $lastSavedProgress")
            mediaPlayer?.pause()
            isPlaying = false
            onPlayStateChanged?.invoke(false, mediaPlayer!!.duration)
            isPlayingLiveData.postValue(false)
            updateNotification()
        }
    }

    fun stop() {
        lastSavedProgress = 0 // 停止时重置进度
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

    // 恢复播放时使用保存的进度
    fun resume() {
        if (currentUrl != null && !isPlaying) {
            try {
                if (lastSavedProgress > 0) {
                    mediaPlayer?.seekTo(lastSavedProgress)
                    Log.d("ProgressSave", "恢复播放，使用进度: $lastSavedProgress")
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
        return if (isPlaying) mediaPlayer?.currentPosition ?: lastSavedProgress else lastSavedProgress
    }

    fun seekTo(position: Int) {
        lastSavedProgress = position
        mediaPlayer?.seekTo(position)
        currentProgress.postValue(position)
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

    // 服务销毁时保存最后进度
    override fun onDestroy() {
        super.onDestroy()
        lastSavedProgress = mediaPlayer?.currentPosition ?: lastSavedProgress
        Log.d("ProgressSave", "服务销毁时保存进度: $lastSavedProgress")
        mediaPlayer?.release()
        mediaPlayer = null
        currentUrl = null
        handler.removeCallbacks(progressUpdateRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val savedProgress = it.getIntExtra("restoreProgress", -1)
            val songId = it.getStringExtra("restoreSongId")
            if (savedProgress != -1 && songId != null && currentSong?.id.toString() == songId) {
                lastSavedProgress = savedProgress
                Log.d("ProgressSave", "从Intent恢复进度: $lastSavedProgress")
            }
        }
        return START_STICKY
    }
}
