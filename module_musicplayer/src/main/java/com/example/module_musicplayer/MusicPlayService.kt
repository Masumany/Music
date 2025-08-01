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
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import data.ListMusicData
import java.lang.ref.WeakReference

class MusicPlayService : Service() {
    // 通知相关常量
    private val CHANNEL_ID = "music_service_channel"
    private val NOTIFICATION_ID = 10086

    // 状态LiveData（供UI观察）
    val currentSongLiveData = MutableLiveData<ListMusicData.Song?>()
    val isPlayingLiveData = MutableLiveData<Boolean>()
    val currentProgress = MutableLiveData<Int>()

    // 媒体播放器核心变量
    private var isMediaPrepared = false
    private var mediaPlayer: MediaPlayer? = null
    var currentUrl: String? = null
    var currentSong: ListMusicData.Song? = null
    var isPlaying = false

    // 进度管理 - 增加volatile保证多线程可见性
    @Volatile var lastSavedProgress = 0  // 保存暂停/停止时的进度
    private var totalDuration = 0  // 歌曲总时长
    private var needRestoreProgress = -1  // 标记是否需要恢复进度

    // 播放列表相关
    var currentPlayList: List<ListMusicData.Song> = emptyList()
    var currentIndex = -1

    // 媒体播放器状态枚举（精细化管理）
    private enum class MediaPlayerState {
        IDLE,          // 初始状态
        INITIALIZED,   // 已设置数据源
        PREPARING,     // 准备中
        PREPARED,      // 已准备
        PLAYING,       // 播放中
        PAUSED,        // 已暂停
        STOPPED,       // 已停止
        ERROR          // 错误状态
    }
    private var mediaPlayerState = MediaPlayerState.IDLE

    // 监听器管理（使用弱引用避免内存泄漏）
    private var onCompletionListener: WeakReference<(() -> Unit)?> = WeakReference(null)
    fun setOnCompletionListener(listener: (() -> Unit)?) {
        onCompletionListener = WeakReference(listener)
    }

    private var onPlayStateChanged: WeakReference<((isPlaying: Boolean, duration: Int) -> Unit)?> = WeakReference(null)
    fun setOnPlayStateChanged(listener: ((isPlaying: Boolean, duration: Int) -> Unit)?) {
        onPlayStateChanged = WeakReference(listener)
    }

    // Binder（供Activity绑定服务）
    inner class MusicBinder : Binder() {
        val service: MusicPlayService
            get() = this@MusicPlayService
    }
    private val binder = MusicBinder()

    // 进度更新任务（定期更新进度）
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null &&
                (mediaPlayerState == MediaPlayerState.PREPARED
                        || mediaPlayerState == MediaPlayerState.PLAYING
                        || mediaPlayerState == MediaPlayerState.PAUSED)) {

                val currentPos = if (isPlaying) {
                    try {
                        mediaPlayer?.currentPosition ?: lastSavedProgress
                    } catch (e: Exception) {
                        Log.e("ProgressUpdate", "获取当前进度失败", e)
                        lastSavedProgress
                    }
                } else {
                    lastSavedProgress
                }

                // 强制更新保存的进度，确保不会丢失
                if (currentPos != lastSavedProgress) {
                    lastSavedProgress = currentPos
                    // 只有播放中才主动通知UI，减少不必要的刷新
                    if (isPlaying) {
                        currentProgress.postValue(currentPos)
                    }
                }
            }
            handler.postDelayed(this, 300)
        }
    }

    // 使用主线程Handler确保UI操作安全
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
        handler.post(progressUpdateRunnable)
        Log.d("ServiceLifeCycle", "服务创建")
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

                setOnPreparedListener { mp ->
                    Log.d("PlayPauseFix", "媒体准备完成，总时长: ${mp.duration}")
                    mediaPlayerState = MediaPlayerState.PREPARED
                    isMediaPrepared = true
                    totalDuration = mp.duration

                    // 准备完成后恢复进度（如果需要）
                    if (needRestoreProgress > 0 && needRestoreProgress < totalDuration) {
                        Log.d("PlayPauseFix", "准备完成，恢复到进度: $needRestoreProgress")
                        try {
                            mp.seekTo(needRestoreProgress)
                            lastSavedProgress = needRestoreProgress
                            // 立即通知UI进度已更新
                            currentProgress.postValue(needRestoreProgress)
                        } catch (e: Exception) {
                            Log.e("PlayPauseFix", "恢复进度失败", e)
                        }
                        needRestoreProgress = -1 // 重置标记
                    }

                    try {
                        mp.start()
                        mediaPlayerState = MediaPlayerState.PLAYING
                        updatePlayingState(true)
                    } catch (e: Exception) {
                        Log.e("PlayInit", "准备完成后播放失败", e)
                        mediaPlayerState = MediaPlayerState.ERROR
                        resetPlayerState()
                        showErrorToast("播放失败")
                    }
                }

                setOnCompletionListener {
                    Log.d("PlayCompletion", "播放完成")
                    mediaPlayerState = MediaPlayerState.STOPPED
                    // 播放完成时保存最终进度
                    lastSavedProgress = totalDuration
                    currentProgress.postValue(lastSavedProgress)
                    resetPlayerState()
                    onCompletionListener.get()?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("PlayError", "播放错误: what=$what, extra=$extra")
                    mediaPlayerState = MediaPlayerState.ERROR

                    val errorMessage = when (what) {
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "未知错误"
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "媒体服务器已断开"
                        MediaPlayer.MEDIA_ERROR_IO -> "网络或文件错误"
                        MediaPlayer.MEDIA_ERROR_MALFORMED -> "媒体格式错误"
                        MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "不支持的媒体格式"
                        MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "播放超时"
                        else -> "错误代码: $what"
                    }

                    showErrorToast("播放失败: $errorMessage")
                    resetPlayerState()
                    true
                }

                setOnSeekCompleteListener {
                    Log.d("SeekComplete", "进度调整完成，当前进度: ${it.currentPosition}")
                    lastSavedProgress = it.currentPosition
                    currentProgress.postValue(lastSavedProgress)
                    if (isPlaying) {
                        mediaPlayerState = MediaPlayerState.PLAYING
                    }
                }
            }
            mediaPlayerState = MediaPlayerState.IDLE
            Log.d("MediaInit", "MediaPlayer初始化完成")
        }
    }

    // 统一更新播放状态的方法
    private fun updatePlayingState(playing: Boolean) {
        isPlaying = playing
        val duration = getDuration()
        // 通知监听器状态变化
        onPlayStateChanged.get()?.invoke(playing, duration)
        isPlayingLiveData.postValue(playing)

        // 处理前台服务
        if (playing) {
            startForegroundWithNotification()
        } else {
            try {
                updateNotification()
            } catch (e: Exception) {
                Log.e("Notification", "更新通知失败", e)
            }
        }
    }

    private fun resetPlayerState() {
        isPlaying = false
        isMediaPrepared = false
        mediaPlayerState = MediaPlayerState.STOPPED
        // 保留最后进度，以便后续恢复
        onPlayStateChanged.get()?.invoke(false, totalDuration)
        isPlayingLiveData.postValue(false)
        currentProgress.postValue(lastSavedProgress)
    }

    // 播放歌曲 - 增加进度恢复机制
    fun play(url: String, song: ListMusicData.Song?) {
        Log.d("PlayControl", "播放歌曲: ${song?.name}, URL: $url")
        val isNewSong = song?.id != currentSong?.id

        // 保存当前歌曲ID用于后续验证
        val currentSongId = song?.id?.toString() ?: ""

        if (isNewSong) {
            // 新歌曲才重置进度
            lastSavedProgress = 0
            needRestoreProgress = -1
            Log.d("PlayControl", "播放新歌曲，重置进度")
        } else {
            // 同一首歌尝试恢复进度
            needRestoreProgress = lastSavedProgress
            Log.d("PlayControl", "继续播放同一首歌，计划恢复进度: $needRestoreProgress")
        }

        currentUrl = url
        currentSong = song
        currentSongLiveData.postValue(song)

        try {
            mediaPlayer?.reset()
            mediaPlayerState = MediaPlayerState.IDLE

            try {
                mediaPlayer?.setDataSource(url)
                mediaPlayerState = MediaPlayerState.INITIALIZED
                mediaPlayer?.prepareAsync()
                mediaPlayerState = MediaPlayerState.PREPARING
            } catch (e: Exception) {
                Log.e("PlayControl", "设置数据源失败", e)
                mediaPlayerState = MediaPlayerState.ERROR
                showErrorToast("无法加载媒体资源")
            }

        } catch (e: Exception) {
            Log.e("PlayControl", "播放初始化失败", e)
            mediaPlayerState = MediaPlayerState.ERROR
            isPlaying = false
            onPlayStateChanged.get()?.invoke(false, 0)
            isPlayingLiveData.postValue(false)
        }
    }

    // 暂停播放
    fun pause() {
        Log.d("PlayPauseFix", "执行暂停，当前进度: ${mediaPlayer?.currentPosition ?: 0}")
        try {
            val mp = mediaPlayer ?: return

            if (mp.isPlaying) {
                // 暂停时强制获取并保存当前进度
                val currentPos = mp.currentPosition
                lastSavedProgress = currentPos
                Log.d("PlayPauseFix", "暂停成功，保存进度: $lastSavedProgress")

                mp.pause()
                mediaPlayerState = MediaPlayerState.PAUSED
                // 立即通知UI进度已更新
                currentProgress.postValue(lastSavedProgress)
                updatePlayingState(false)
            } else {
                Log.d("PlayPauseFix", "暂停未执行: 播放器未在播放状态")
            }
        } catch (e: Exception) {
            Log.e("PlayPauseFix", "暂停失败", e)
            // 异常时仍尝试保存当前已知进度
            lastSavedProgress = mediaPlayer?.currentPosition ?: lastSavedProgress
            currentProgress.postValue(lastSavedProgress)
            mediaPlayerState = MediaPlayerState.ERROR
            resetPlayerState()
        }
    }

    // 停止播放
    fun stop() {
        Log.d("PlayControl", "执行停止，当前进度: ${mediaPlayer?.currentPosition ?: lastSavedProgress}")
        // 停止时保存当前进度
        lastSavedProgress = mediaPlayer?.currentPosition ?: lastSavedProgress
        currentProgress.postValue(lastSavedProgress)

        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                mediaPlayerState = MediaPlayerState.STOPPED
            } catch (e: Exception) {
                Log.e("PlayControl", "停止操作失败", e)
                mediaPlayerState = MediaPlayerState.ERROR
            }
        }
        isPlaying = false
        onPlayStateChanged.get()?.invoke(false, totalDuration)
        isPlayingLiveData.postValue(false)
    }

    // 恢复播放
    fun resume() {
        Log.d("PlayPauseFix", "执行恢复播放，保存的进度: $lastSavedProgress")
        try {
            val mp = mediaPlayer ?: return

            if (!isPlaying) {
                // 恢复时强制跳转到保存的进度
                if (lastSavedProgress > 0) {
                    Log.d("PlayPauseFix", "恢复到进度: $lastSavedProgress")
                    try {
                        // 先暂停再跳转，确保稳定性
                        if (mp.isPlaying) mp.pause()
                        mp.seekTo(lastSavedProgress)
                    } catch (e: Exception) {
                        Log.e("PlayPauseFix", "跳转进度失败", e)
                        // 如果跳转失败，标记需要在准备完成后恢复
                        needRestoreProgress = lastSavedProgress
                    }
                }

                mp.start()
                mediaPlayerState = MediaPlayerState.PLAYING
                updatePlayingState(true)
            }
        } catch (e: Exception) {
            Log.e("PlayPauseFix", "恢复播放失败", e)
            mediaPlayerState = MediaPlayerState.ERROR
            // 从保存的进度重新播放
            if (lastSavedProgress > 0 && !currentUrl.isNullOrBlank()) {
                Log.d("PlayPauseFix", "尝试从错误中恢复播放")
                needRestoreProgress = lastSavedProgress
                play(currentUrl!!, currentSong)
            } else {
                resetPlayerState()
                showErrorToast("播放失败，请重试")
            }
        }
    }

    // 获取当前播放位置
    @Synchronized
    fun getCurrentPosition(): Int {
        return if (isPlaying) {
            try {
                val currentPos = mediaPlayer?.currentPosition ?: lastSavedProgress
                // 实时更新保存的进度
                if (currentPos != lastSavedProgress) {
                    lastSavedProgress = currentPos
                }
                currentPos
            } catch (e: Exception) {
                Log.e("Position", "获取当前位置失败", e)
                lastSavedProgress
            }
        } else {
            lastSavedProgress
        }
    }

    // 调整进度
    fun seekTo(position: Int) {
        Log.d("SeekControl", "调整到进度: $position")
        if (mediaPlayer != null && (mediaPlayerState == MediaPlayerState.PLAYING
                    || mediaPlayerState == MediaPlayerState.PAUSED
                    || mediaPlayerState == MediaPlayerState.PREPARED)) {

            val validPosition = when {
                position < 0 -> 0
                position > totalDuration -> totalDuration
                else -> position
            }

            // 立即更新保存的进度
            lastSavedProgress = validPosition
            currentProgress.postValue(validPosition)

            try {
                mediaPlayer?.seekTo(validPosition)
            } catch (e: Exception) {
                Log.e("SeekControl", "调整进度失败", e)
                showErrorToast("无法调整进度")
            }
        } else {
            Log.w("SeekControl", "无法调整进度，当前状态: $mediaPlayerState")
            lastSavedProgress = position
            currentProgress.postValue(position)
        }
    }

    // 获取总时长
    fun getDuration(): Int {
        if (mediaPlayerState == MediaPlayerState.PREPARED
            || mediaPlayerState == MediaPlayerState.PLAYING
            || mediaPlayerState == MediaPlayerState.PAUSED) {
            return try {
                mediaPlayer?.duration ?: 0
            } catch (e: Exception) {
                Log.e("Duration", "获取时长失败", e)
                0
            }
        } else {
            Log.w("Duration", "在状态${mediaPlayerState}下获取时长")
            return totalDuration
        }
    }

    // 显示错误提示
    private fun showErrorToast(message: String) {
        handler.post {
            Toast.makeText(this@MusicPlayService, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 前台服务处理
    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithNotification() {
        try {
            val notification = createNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } catch (e: IllegalArgumentException) {
                    Log.w("Foreground", "指定类型启动失败，尝试兼容模式", e)
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("Foreground", "启动前台服务失败", e)
            showErrorToast("后台播放可能不稳定")
        }
    }

    // 创建通知时携带当前进度
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
        // 显示当前进度/总时长
        val progressText = "${formatTime(lastSavedProgress)} / ${formatTime(totalDuration)}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText("$playStateText · $artistName")
            .setSubText(progressText) // 显示进度信息
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    // 格式化时间显示
    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
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

    // 服务销毁时强制保存进度
    override fun onDestroy() {
        super.onDestroy()
        // 保存最后进度
        lastSavedProgress = try {
            mediaPlayer?.currentPosition ?: lastSavedProgress
        } catch (e: Exception) {
            Log.e("Destroy", "保存进度失败", e)
            lastSavedProgress
        }
        Log.d("Destroy", "服务销毁，保存最后进度: $lastSavedProgress")

        // 进度保存到SharedPreferences
        currentSong?.id?.toString()?.let { songId ->
            getSharedPreferences("MusicServicePrefs", MODE_PRIVATE)
                .edit()
                .putInt("last_progress_$songId", lastSavedProgress)
                .apply()
        }

        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("Destroy", "释放播放器失败", e)
        }
        mediaPlayer = null
        currentUrl = null
        handler.removeCallbacks(progressUpdateRunnable)
        onCompletionListener = WeakReference(null)
        onPlayStateChanged = WeakReference(null)
    }

    // 增强进度恢复逻辑
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val savedProgress = it.getIntExtra("restoreProgress", -1)
            val songId = it.getStringExtra("restoreSongId")
            if (savedProgress != -1 && songId != null) {
                // 即使当前没有歌曲，也先保存进度，等歌曲加载后恢复
                if (currentSong?.id.toString() == songId || currentSong == null) {
                    lastSavedProgress = savedProgress
                    needRestoreProgress = savedProgress
                    Log.d("Restore", "恢复进度: $lastSavedProgress")
                }
            }
        }
        return START_STICKY
    }
}
