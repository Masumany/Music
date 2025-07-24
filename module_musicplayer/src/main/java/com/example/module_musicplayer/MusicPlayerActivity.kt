package com.example.module_musicplayer

import Adapter.MusicDataCache
import Event.CloseLyricEvent
import Event.PlayProgressEvent
import android.animation.Animator
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.lib.base.NetWorkClient
import com.example.lib.base.Song
import com.example.module_musicplayer.databinding.MusicPlayerBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import data.ListMusicData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

@Route(path = "/module_musicplayer/musicplayer")
class MusicPlayerActivity : AppCompatActivity() {

    @JvmField
    @Autowired
    var currentPosition: Int? = null

    @Autowired
    @JvmField
    var id: String? = null

    @Autowired
    @JvmField
    var cover: String? = null

    @Autowired
    @JvmField
    var songName: String? = null

    @Autowired
    @JvmField
    var song: Song? = null

    @Autowired
    @JvmField
    var songListName: String? = null

    @Autowired
    @JvmField
    var athour: String? = null

    @Autowired
    @JvmField
    var playProgress: Int = 0

    @JvmField
    @Autowired
    var singerId: Long? = 0

    // 是否需要应用传递的进度
    private var needApplyPlayProgress = false

    // 进度相关变量（使用Int类型与服务保持一致）
    private var lastPausedProgress = 0
    private var totalDuration = 0

    private lateinit var binding: MusicPlayerBinding
    private var fragment: Fragment? = null
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 90f
    private var currentRotation = ROTATION_PAUSED
    private var animation: Animation? = null
    private var isLiked = false
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var currentMusic: String? = null
    var musicList: List<ListMusicData.Song>? = null
    var currentIndex = -1

    private val MODE_SEQUENTIAL = 0
    private val MODE_SINGLE = 1
    private val MODE_RANDOM = 2
    private var currentMode = MODE_SEQUENTIAL

    private var musicService: MusicPlayService? = null
    private var isServiceBound = false
    private var isServiceReady = false
    private var pendingPlayTask: (() -> Unit)? = null

    private val isPreparePlayNext = false

    // 服务连接的回调
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayService.MusicBinder
            musicService = binder.service
            isServiceBound = true
            isServiceReady = true

            syncServiceStateAfterConnection()


            // 初始化进度
            lastPausedProgress = musicService?.lastSavedProgress ?: 0
            totalDuration = musicService?.getDuration() ?: 0
            updateProgressUI(lastPausedProgress, totalDuration)

            // 同步播放状态到按钮
            updatePlayButtonState(musicService?.isPlaying == true)

            // 监听播放状态变化（包含总时长）
            musicService?.setOnPlayStateChanged { isPlaying, duration ->
                runOnUiThread {
                    totalDuration = duration
                    updatePlayButtonState(isPlaying)
                    updateProgressUI(lastPausedProgress, totalDuration)
                    updateAnimationState(isPlaying)
                }
            }

            // 监听进度变化
            musicService?.currentProgress?.observe(this@MusicPlayerActivity) { progress ->
                // 只有在非拖动状态且播放中才更新UI
                if (musicService?.isPlaying == true && !binding.mpSeekBar.isPressed) {
                    lastPausedProgress = progress
                    updateProgressUI(progress, totalDuration)
                }
            }

            // 监听播放完成
            musicService?.setOnCompletionListener {
                runOnUiThread {
                    if (!isPreparePlayNext) {
                        playNextSong()
                    }
                }
            }

            // 歌手信息点击
            binding.mpSinger.setOnClickListener {
                val targetSingerId = if (currentIndex in musicList?.indices ?: emptyList()) {
                    musicList!![currentIndex].ar.firstOrNull()?.id ?: 0L
                } else {
                    singerId?.toLong() ?: 0L
                }

                if (targetSingerId <= 0) {
                    val errorMsg = "无法获取有效歌手ID（当前值：$targetSingerId）"
                    Toast.makeText(this@MusicPlayerActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("MusicPlayer", errorMsg)
                    return@setOnClickListener
                }

                TheRouter.build("/singer/SingerActivity")
                    .withLong("id", targetSingerId)
                    .navigation()
            }

            // 评论点击
            binding.mpCommon.setOnClickListener {
                val currentSongId = if (currentIndex in musicList?.indices ?: emptyList()) {
                    musicList!![currentIndex].id.toString()
                } else {
                    id ?: ""
                }

                if (currentSongId.isBlank()) {
                    Toast.makeText(
                        this@MusicPlayerActivity,
                        "无法获取歌曲ID，无法查看评论",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                TheRouter.build("/module_musicplayer/commentActivity")
                    .withString("id", currentSongId)
                    .navigation()
            }

            // 歌词点击
            binding.mpCenter.setOnClickListener {
                val currentSongId = if (currentIndex in musicList?.indices ?: emptyList()) {
                    musicList!![currentIndex].id.toString()
                } else {
                    id ?: ""
                }
                if (currentSongId.isBlank()) {
                    Toast.makeText(
                        this@MusicPlayerActivity,
                        "无法获取歌曲ID，无法查看歌词",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                TheRouter.build("/song/SongWord")
                    .withString("id", currentSongId)
                    .withInt("currentPos", lastPausedProgress)
                    .navigation()
            }

            // 执行等待中的播放任务
            pendingPlayTask?.invoke()
            pendingPlayTask = null
        }

        private fun syncServiceStateAfterConnection() {
            if (musicService == null) return

            // 强制同步播放状态到按钮
            val serviceIsPlaying = musicService?.isPlaying == true
            Log.d("PlayButtonDebug", "服务连接后同步状态: $serviceIsPlaying")
            updatePlayButtonState(serviceIsPlaying)

            // 同步进度
            lastPausedProgress = musicService?.lastSavedProgress ?: 0
            totalDuration = musicService?.getDuration() ?: 0
            updateProgressUI(lastPausedProgress, totalDuration)

            // 同步动画状态
            updateAnimationState(serviceIsPlaying)
        }


        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            isServiceReady = false
        }
    }

    // 更新进度UI
    private fun updateProgressUI(currentProgress: Int, totalDuration: Int) {
        binding.mpSeekBar.max = totalDuration
        binding.mpSeekBar.progress = currentProgress
        binding.mpTimestart.text = formatTime(currentProgress)
        binding.mpTimend.text = formatTime(totalDuration)
    }

    // 格式化时间（毫秒转分:秒）
    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // 进度条更新任务
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isServiceReady && musicService?.isPlaying == true && !binding.mpSeekBar.isPressed) {
                lastPausedProgress = musicService?.getCurrentPosition() ?: lastPausedProgress
                updateProgressUI(lastPausedProgress, totalDuration)

                if(totalDuration>0&&lastPausedProgress>=(totalDuration-5000)&&!isPreparePlayNext){
                    playNextSong()
                }
            }
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mpName.requestFocus()

        TheRouter.inject(this)
        Log.d("MusicPlayerActivity", "接收的歌手ID: $singerId")
        needApplyPlayProgress = playProgress > 0
        Log.d("MusicPlayer", "初始化参数: 进度=$playProgress，是否应用=$needApplyPlayProgress")

        // 初始化播放模式
        restorePlayMode()
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED

        // 绑定服务
        val serviceIntent = Intent(this, MusicPlayService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        // 初始化事件监听
        initEventListeners()
        initCenterImg()
        initSeekBarListener()
        initPlayButton()
        initNextSong()
        initLastSong()
        updateModeIcon()
        initProgressEventSender()

        // 初始化歌曲信息和列表
        initMusicInfo()
        initFragment()
        fetchMusic()

        // 启动进度更新
        handler.post(updateSeekBarRunnable)
    }

    // 进度事件发送器
    private fun initProgressEventSender() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isServiceReady && musicService != null) {
                    val currentSong = musicList?.getOrNull(currentIndex)
                    val currentSongId = currentSong?.id
                    val currentProgress = musicService?.getCurrentPosition() ?: 0

                    EventBus.getDefault().post(
                        currentPosition?.let {
                            PlayProgressEvent(
                                songId = currentSongId.toString(),
                                position = currentProgress,
                                duration = musicService?.getDuration() ?: 0,
                            )
                        }
                    )
                }
                handler.postDelayed(this, 100)
            }
        }, 100)
    }

    // 恢复播放模式
    private fun restorePlayMode() {
        val prefs: SharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        currentMode = prefs.getInt("play_mode", MODE_SEQUENTIAL)
    }

    // 保存播放模式
    private fun savePlayMode() {
        val prefs: SharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        prefs.edit().putInt("play_mode", currentMode).apply()
    }

    // 更新播放模式图标
    private fun updateModeIcon() {
        val iconRes = when (currentMode) {
            MODE_SEQUENTIAL -> R.drawable.play_count
            MODE_SINGLE -> R.drawable.mp_one
            MODE_RANDOM -> R.drawable.mp_change
            else -> R.drawable.play_count
        }
        binding.mpCount.setImageResource(iconRes)
    }

    // 切换播放模式
    private fun changeMode() {
        currentMode = (currentMode + 1) % 3
        updateModeIcon()
        savePlayMode()

        val modeName = when (currentMode) {
            MODE_SEQUENTIAL -> "顺序播放"
            MODE_SINGLE -> "单曲循环"
            MODE_RANDOM -> "随机播放"
            else -> "顺序播放"
        }
        Toast.makeText(this, "已切换为$modeName", Toast.LENGTH_SHORT).show()
    }

    // 初始化歌曲信息UI
    private fun initMusicInfo() {
        song?.let {
            updateMusicInfo(it.name, it.al.picUrl ?: "", it.ar.joinToString { a -> a.name })
        } ?: run {
            val safeSongName = songListName ?: "未知歌曲"
            val safeCover = cover ?: ""
            updateMusicInfo(safeSongName, safeCover)
        }
    }

    // 初始化列表Fragment
    private fun initFragment() {
        fragment = supportFragmentManager.findFragmentById(binding.mpFragment.id)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().hide(fragment!!).commit()
        } else {
            Log.w("MusicPlayer", "Fragment容器为空")
        }
    }

    // 上一首按钮监听
    private fun initLastSong() {
        binding.mpLast.setOnClickListener {
            playLastSong()
        }
    }

    // 下一首按钮监听
    private fun initNextSong() {
        binding.mpNext.setOnClickListener {
            playNextSong()
        }
    }

    // 通用事件监听
    private fun initEventListeners() {
        // 返回按钮
        binding.mpMback.setOnClickListener {
            musicService?.lastSavedProgress = lastPausedProgress
            finish()
        }

        // 列表按钮
        binding.mpList.setOnClickListener {
            fragment?.let { changeFragment() }
                ?: Toast.makeText(this, "列表加载失败", Toast.LENGTH_SHORT).show()
        }

        // 收藏按钮
        binding.mpLike.setOnClickListener {
            isLiked = !isLiked
            binding.mpLike.setImageResource(if (isLiked) R.drawable.like_last else R.drawable.like)
            Toast.makeText(this, if (isLiked) "已收藏" else "已取消", Toast.LENGTH_SHORT).show()
        }

        // 播放模式按钮
        binding.mpCount.setOnClickListener {
            changeMode()
        }

        // 分享按钮
        binding.mpShare.setOnClickListener {
            val audioUri = getCurrentAudioUri()
            if (audioUri == null) {
                Toast.makeText(this, "无法获取音频文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, audioUri)
                putExtra(Intent.EXTRA_TEXT, "推荐一首好歌：${binding.mpName.text}")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(shareIntent, "分享音频到"))
        }
    }

    // 获取音频文件URI
    private fun getCurrentAudioUri(): Uri? {
        return try {
            currentMusic?.let { url ->
                Uri.parse(url)
            }
        } catch (e: Exception) {
            Log.e("AudioShare", "获取音频URI失败", e)
            null
        }
    }

    // 根据歌曲ID查找索引
    private fun findSongIndexById(targetId: String): Int {
        if (targetId.isBlank() || musicList.isNullOrEmpty()) return -1
        return try {
            val targetIdLong = targetId.toLong()
            musicList?.indexOfFirst { it.id == targetIdLong } ?: -1
        } catch (e: NumberFormatException) {
            Log.e("MusicPlayer", "ID格式错误: $targetId")
            -1
        }
    }

    // 检查并播放当前歌曲
    fun playCurrentMusicWithCheck() {
        if (musicList?.indices?.contains(currentIndex) == true) {
            val currentSong = musicList!![currentIndex]
            updateMusicInfo(
                songName = currentSong.name,
                coverUrl = currentSong.al.picUrl ?: "",
                singer = currentSong.ar.joinToString { it.name }
            )

            // 切换歌曲时重置进度
            lastPausedProgress = 0
            musicService?.stop()
            binding.mpSeekBar.progress = 0
            binding.mpTimestart.text = formatTime(0)
            currentMusic = null

            fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
        } else {
            Toast.makeText(this, "歌曲不存在", Toast.LENGTH_SHORT).show()
            updatePlayButtonState(false)
        }
    }

    // 获取播放地址并播放
    private fun fetchAndPlayWithStateCheck(songId: String, songName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val musicUrlResponse = NetWorkClient.apiService3.getMusicUrl(songId)
                withContext(Dispatchers.Main) {
                    if (musicUrlResponse.code == 200 && musicUrlResponse.data != null && musicUrlResponse.data.isNotEmpty()) {
                        val musicUrl = musicUrlResponse.data[0].url
                        if (!musicUrl.isNullOrBlank()) {
                            currentMusic = musicUrl
                            if (isServiceReady) {
                                playAudio(musicUrl) { success ->
                                    binding.mpStart.isEnabled = true
                                    updatePlayButtonState(success)
                                }
                            } else {
                                pendingPlayTask = {
                                    playAudio(musicUrl) { success ->
                                        binding.mpStart.isEnabled = true
                                        updatePlayButtonState(success)
                                    }
                                }
                                Toast.makeText(
                                    this@MusicPlayerActivity,
                                    "服务初始化中，请稍候...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            handleSongError(songName)
                        }
                    } else {
                        handleSongError(songName)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SongError", "获取歌曲失败: ${e.message}")
                    handleSongError(songName)
                }
            }
        }
    }

    // 处理歌曲播放错误
    private fun handleSongError(songName: String) {
        Toast.makeText(this, "歌曲《$songName》无法播放", Toast.LENGTH_SHORT).show()
        updatePlayButtonState(false)
        musicService?.stop()
        lastPausedProgress = 0
        stopCenterImg()
        if (currentRotation != ROTATION_PAUSED) {
            rotateToPaused()
        }
    }

    // 播放上一首
    private fun playLastSong() {
        EventBus.getDefault().post(CloseLyricEvent())
        if (musicList.isNullOrEmpty()) {
            Toast.makeText(this, "没有上一首", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex - 1 + musicList!!.size) % musicList!!.size
            }

            MODE_SINGLE -> {
                musicService?.seekTo(0)
                lastPausedProgress = 0
                binding.mpSeekBar.progress = 0
                binding.mpTimestart.text = formatTime(0)
                return
            }

            MODE_RANDOM -> {
                if (musicList!!.size <= 1) {
                    currentIndex = 0
                } else {
                    var randomIndex: Int
                    do {
                        randomIndex = Random.nextInt(musicList!!.size)
                    } while (randomIndex == currentIndex)
                    currentIndex = randomIndex
                }
            }
        }

        playCurrentMusicWithCheck()
    }

    // 播放下一首
    private fun playNextSong() {
        EventBus.getDefault().post(CloseLyricEvent())
        if (musicList.isNullOrEmpty()) {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex + 1) % musicList!!.size
            }

            MODE_SINGLE -> {
                musicService?.seekTo(0)
                lastPausedProgress = 0
                binding.mpSeekBar.progress = 0
                binding.mpTimestart.text = formatTime(0)
                playCurrentMusicWithCheck()
                return
            }

            MODE_RANDOM -> {
                if (musicList!!.size <= 1) {
                    currentIndex = 0
                } else {
                    var randomIndex: Int
                    do {
                        randomIndex = Random.nextInt(musicList!!.size)
                    } while (randomIndex == currentIndex)
                    currentIndex = randomIndex
                }
            }
        }

        playCurrentMusicWithCheck()
    }

    // 获取歌曲列表并初始化播放
    private fun fetchMusic() {
        musicList = MusicDataCache.currentSongList ?: emptyList()
        val songList = MusicDataCache.currentSongList
        if (songList.isNullOrEmpty()) {
            Toast.makeText(this, "未找到歌曲列表", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (songList != null && songList.isNotEmpty()) {
            musicList = songList
            currentIndex = currentPosition ?: 0
            Log.d("MusicPlayer", "列表大小: ${songList.size}, 当前索引: $currentIndex")

            if (needApplyPlayProgress && musicList?.indices?.contains(currentIndex) == true) {
                val currentSong = musicList!![currentIndex]
                updateMusicInfo(
                    songName = currentSong.name,
                    coverUrl = currentSong.al.picUrl ?: "",
                    singer = currentSong.ar.joinToString { it.name }
                )
                Log.d("MusicPlayer", "等待服务连接后应用进度: $playProgress")
            } else {
                if (musicList?.indices?.contains(currentIndex) == true) {
                    fetchAndPlayWithStateCheck(
                        musicList!![currentIndex].id.toString(),
                        musicList!![currentIndex].name
                    )
                } else {
                    Log.d("MusicPlayerActivity", "currentIndex out of range")
                }
            }
        } else {
            // 加载默认列表
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dailySongsResponse = NetWorkClient.apiService2.getListData()
                    if (dailySongsResponse.code == 200 && !dailySongsResponse.data?.dailySongs.isNullOrEmpty()) {
                        musicList =
                            dailySongsResponse.data?.dailySongs as? List<ListMusicData.Song>?
                        withContext(Dispatchers.Main) {
                            currentIndex = findSongIndexById(id ?: "")
                            if (currentIndex == -1) currentIndex = 0
                            fetchAndPlayWithStateCheck(
                                musicList!![currentIndex].id.toString(),
                                musicList!![currentIndex].name
                            )
                        }
                    } else {
                        throw Exception("获取歌曲列表失败")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("SongError", "获取歌曲列表失败: ${e.message}")
                    }
                }
            }
        }
    }

    // 更新歌曲信息UI
    private fun updateMusicInfo(songName: String, coverUrl: String, singer: String? = null) {
        binding.mpName.text = songName
        binding.mpSinger.text = singer ?: athour ?: "未知歌手"
        Glide.with(this)
            .load(coverUrl)
            .error(R.drawable.mp_music)
            .into(binding.mpCenter)
    }

    // 切换列表Fragment显示状态
    private fun changeFragment() {
        fragment?.let { frag ->
            val transaction = supportFragmentManager.beginTransaction()
            if (frag.isHidden) {
                transaction.show(frag)
            } else {
                transaction.hide(frag)
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.commit()
        }
    }

    // 隐藏列表Fragment
    fun hideFragment() {
        fragment?.let {
            if (it.isVisible) {
                supportFragmentManager.beginTransaction().hide(it).commit()
            }
        }
    }

    // 初始化中心图片动画
    private fun initCenterImg() {
        animation = AnimationUtils.loadAnimation(this, R.anim.img_animation).apply {
            interpolator = LinearInterpolator()
        }
    }

    // 初始化进度条监听（修复拖动问题）
    private fun initSeekBarListener() {
        binding.mpSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 实时更新进度文本
                    binding.mpTimestart.text = formatTime(progress)
                    // 立即同步到服务
                    if (isServiceReady) {
                        musicService?.seekTo(progress)
                    }
                    // 更新本地进度记录
                    lastPausedProgress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 拖动开始时暂停自动更新
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 拖动结束后恢复自动更新
                if (isServiceReady && musicService?.isPlaying == true) {
                    handler.post(updateSeekBarRunnable)
                }
                // 确保最终进度同步
                seekBar?.progress?.let {
                    if (isServiceReady) {
                        musicService?.seekTo(it)
                    }
                }
            }
        })
    }

    // 初始化播放按钮（修复状态问题）
    private fun initPlayButton() {
        // 确保按钮点击事件正确注册
        binding.mpStart.setOnClickListener {
            handlePlayClick()
        }
        // 初始状态设置为暂停图标
        updatePlayButtonState(false)
    }

    // 处理播放/暂停点击
    private fun handlePlayClick() {
        Log.d("PlayLogic", "进入handlePlayClick()")

        if (!isServiceBound) {
            Log.e("PlayLogic", "服务未绑定，尝试重新绑定")
            val serviceIntent = Intent(this, MusicPlayService::class.java)
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
            Toast.makeText(this, "服务连接中...", Toast.LENGTH_SHORT).show()
            return
        }

        if (musicService == null) {
            Log.e("PlayLogic", "musicService为null，无法执行操作")
            Toast.makeText(this, "播放器未就绪", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val isPlaying = musicService?.isPlaying == true
            Log.d("PlayLogic", "当前播放状态: $isPlaying")

            // 点击后立即更新按钮图标，提供即时反馈
            val newState = !isPlaying
            updatePlayButtonState(newState)

            if (isPlaying) {
                // 执行暂停
                musicService?.pause()
                Log.d("PlayLogic", "已调用pause()")
            } else {
                // 执行播放
                if (musicService?.currentUrl.isNullOrBlank()) {
                    Log.d("PlayLogic", "无当前播放URL，执行首次播放")
                    if (currentIndex in musicList?.indices ?: emptyList()) {
                        val currentSong = musicList!![currentIndex]
                        fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
                    } else {
                        Log.e("PlayLogic", "当前索引无效，无法播放")
                        Toast.makeText(this, "无可用歌曲", Toast.LENGTH_SHORT).show()
                        // 如果播放失败，恢复按钮状态
                        updatePlayButtonState(false)
                    }
                } else {
                    Log.d("PlayLogic", "有当前播放URL，执行resume()")
                    musicService?.resume()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayLogic", "处理播放点击时发生异常", e)
            Toast.makeText(this, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            // 异常时恢复按钮状态
            updatePlayButtonState(musicService?.isPlaying == true)
        }
    }

    // 更新播放按钮状态
    private fun updatePlayButtonState(isPlaying: Boolean) {
        // 使用runOnUiThread确保在主线程执行UI更新
        runOnUiThread {
            val iconRes = if (isPlaying) R.drawable.mp_stop else R.drawable.mp_start
            // 强制设置图标
            binding.mpStart.setImageResource(iconRes)
            // 强制视图刷新
            binding.mpStart.invalidate()
            Log.d("ButtonUpdate", "按钮状态已更新为: ${if (isPlaying) "暂停" else "播放"}")
        }
    }

    // 旋转到播放状态
    private fun rotateToPlaying() {
        binding.mpRecord.post {
            binding.mpRecord.animate().cancel()
            val anim = binding.mpRecord.animate()
                .rotation(ROTATION_PLAYING)
                .setDuration(500)

            anim.interpolator = LinearInterpolator()

            anim.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    currentRotation = ROTATION_PLAYING
                    anim.setListener(null)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.start()
        }
    }

    // 旋转到暂停状态
    private fun rotateToPaused() {
        binding.mpRecord.post {
            binding.mpRecord.animate().cancel()
            val anim = binding.mpRecord.animate()
                .rotation(ROTATION_PAUSED)
                .setDuration(500)

            anim.interpolator = LinearInterpolator()

            anim.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    currentRotation = ROTATION_PAUSED
                    anim.setListener(null)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.start()
        }
    }

    // 更新动画状态
    private fun updateAnimationState(isPlaying: Boolean) {
        if (isPlaying) {
            startCenterImg()
            if (currentRotation != ROTATION_PLAYING) {
                rotateToPlaying()
            }
        } else {
            stopCenterImg()
            if (currentRotation != ROTATION_PAUSED) {
                rotateToPaused()
            }
        }
    }

    // 启动中心图片动画
    private fun startCenterImg() {
        animation?.let {
            binding.mpCenter.startAnimation(it)
        }
    }

    // 停止中心图片动画
    private fun stopCenterImg() {
        animation?.let {
            binding.mpCenter.clearAnimation()
        }
    }

    // 播放音频
    private fun playAudio(url: String, onPrepared: (Boolean) -> Unit) {
        if (!isServiceReady || musicService == null) {
            onPrepared(false)
            return
        }

        musicService?.setOnPlayStateChanged(null)
        musicService?.setOnCompletionListener {
            runOnUiThread {
                playNextSong()
            }
        }

        musicService?.setOnPlayStateChanged { isPlaying, duration ->
            runOnUiThread {
                totalDuration = duration
                // 确保按钮状态与服务状态一致
                updatePlayButtonState(isPlaying)
                updateProgressUI(lastPausedProgress, totalDuration)
                updateAnimationState(isPlaying)
            }
        }

        try {
            val currentSong = musicList?.getOrNull(currentIndex)
                ?: throw Exception("当前歌曲不存在于列表中")

            musicService?.play(
                url,
                song = currentSong
            )
        } catch (e: Exception) {
            Log.e("AudioError", "播放异常: ${e.message}")
            onPrepared(false)
        }
    }

    // 销毁时清理资源
    override fun onDestroy() {
        super.onDestroy()
        // 保存最后进度
        musicService?.lastSavedProgress = lastPausedProgress
        Log.d("ProgressSave", "Activity销毁时保存进度: $lastPausedProgress")

        // 停止进度更新
        handler.removeCallbacksAndMessages(null)

        // 清除监听器，避免内存泄漏
        musicService?.setOnCompletionListener(null)
        musicService?.setOnPlayStateChanged(null)

        // 解除服务绑定
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }

        stopCenterImg()
    }
}
