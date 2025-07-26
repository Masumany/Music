package com.example.module_musicplayer

import Adapter.MusicDataCache
import Event.CloseCommentEvent
import Event.CloseLyricEvent
import Event.PlayProgressEvent
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.lib.base.NetworkClient
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

    // 当前播放列表相关
    private var currentPlayList: List<ListMusicData.Song>? = null
    private var currentIndex = -1
    private var listFragment: ListFragment? = null

    //其他的一些
    private var needApplyPlayProgress = false
    private var lastPausedProgress = 0
    private var totalDuration = 0
    private lateinit var binding: MusicPlayerBinding
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 90f
    private var currentRotation = ROTATION_PAUSED
    private var isLiked = false
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var currentMusic: String? = null

    // 动画相关
    private var centerRotationAnimator: ObjectAnimator? = null
    private var currentCenterRotation = 0f

    // 播放模式
    private val MODE_SEQUENTIAL = 0
    private val MODE_SINGLE = 1
    private val MODE_RANDOM = 2
    private var currentMode = MODE_SEQUENTIAL

    // 服务相关
    private var musicService: MusicPlayService? = null
    private var isServiceBound = false
    private var isServiceReady = false
    private var pendingPlayTask: (() -> Unit)? = null  //等待执行播放的任务
    private val isPreparePlayNext = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayService.MusicBinder
            musicService = binder.service
            isServiceBound = true
            isServiceReady = true

            // 同步服务中的播放列表
            currentPlayList = musicService?.currentPlayList
            currentIndex = musicService?.currentIndex ?: currentIndex

            syncServiceStateAfterConnection()
            setupServiceListeners()
            updateFragmentPlayList() // 更新列表Fragment
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            isServiceReady = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mpName.requestFocus()

        TheRouter.inject(this)
        needApplyPlayProgress = playProgress > 0

        // 初始化播放模式
        restorePlayMode()
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED

        // 绑定服务
        val serviceIntent = Intent(this, MusicPlayService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        // 初始化组件
        initEventListeners()
        initCenterImgAnimation()
        initSeekBarListener()
        initPlayButton()
        initNextSong()
        initLastSong()
        updateModeIcon()
        initProgressEventSender()
        initFragment() // 初始化列表Fragment
        initMusicInfo()
        // 延迟初始化播放列表，确保Fragment有足够的时间去创建
        handler.postDelayed({
            initCurrentPlayList()
        }, 300)

        // 启动进度更新
        handler.post(updateSeekBarRunnable)
    }

    // 初始化列表Fragment
    private fun initFragment() {
        listFragment = ListFragment()
        val transaction = supportFragmentManager.beginTransaction()
            .replace(binding.mpFragment.id, listFragment!!)
        transaction.commit()
        // 立即执行事务，确保Fragment尽快初始化
        supportFragmentManager.executePendingTransactions()

        // 设置列表点击监听
        listFragment?.setOnSongSelectListener { position ->
            playSongFromList(position)
        }
    }

    // 更新列表Fragment数据
    private fun updateFragmentPlayList() {
        if (listFragment?.isAdded == true) {
            val songs = currentPlayList?.map {
                Song(
                    id = it.id,
                    name = it.name,
                    ar = it.ar,
                    al = it.al
                )
            } ?: emptyList()
            listFragment?.updatePlayList(songs, currentIndex)
        } else {
            Log.w("MusicPlayerActivity", "Fragment未准备好，暂不更新列表")
            // 延迟重试
            handler.postDelayed({
                updateFragmentPlayList()
            }, 200)
        }
    }

    // 从列表选择歌曲播放
    fun playSongFromList(index: Int) {
        if (currentPlayList.isNullOrEmpty() || index < 0 || index >= currentPlayList!!.size) {
            return
        }
        currentIndex = index
        musicService?.currentIndex = index
        playCurrentMusicWithCheck()
        hideFragment() // 隐藏列表
    }

    // 隐藏Fragment
    fun hideFragment() {
        listFragment?.let {
            if (it.isAdded) {
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(it)
                    .commit()
            }
        }
    }

    // 显示Fragment
    private fun showFragment() {
        listFragment?.let {
            if (it.isAdded) {
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .show(it)
                    .commit()
            }
        }
    }

    // 初始化当前播放列表
    private fun initCurrentPlayList() {
        // 先使用缓存列表
        currentPlayList = MusicDataCache.currentSongList
        if (currentPlayList != null && currentPlayList!!.isNotEmpty()) {
            currentIndex = currentPosition ?: findSongIndexById(id ?: "")
            if (currentIndex < 0 || currentIndex >= currentPlayList!!.size) {
                currentIndex = 0
            }
            Log.d("PlayListInit", "使用缓存列表, 大小: ${currentPlayList!!.size}")
            startPlayCurrentSong()
            return
        }

        // 加载默认列表
        Log.d("PlayListInit", "加载默认列表")
        loadDefaultPlayList()
    }

    // 加载默认播放列表
    private fun loadDefaultPlayList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dailySongsResponse = NetworkClient.apiService.getDailyRecommendSongs()
                if (dailySongsResponse.code == 200 && !dailySongsResponse.data?.dailySongs.isNullOrEmpty()) {
                    currentPlayList =
                        dailySongsResponse.data?.dailySongs as? List<ListMusicData.Song>?
                    withContext(Dispatchers.Main) {
                        if (currentPlayList != null && currentPlayList!!.isNotEmpty()) {
                            // 缓存当前列表
                            MusicDataCache.currentSongList = currentPlayList
                            currentIndex = findSongIndexById(id ?: "")
                            if (currentIndex < 0 || currentIndex >= currentPlayList!!.size) {
                                currentIndex = 0
                            }
                            startPlayCurrentSong()
                        } else {
                            throw Exception("获取默认列表为空")
                        }
                    }
                } else {
                    throw Exception("获取默认列表失败，code: ${dailySongsResponse.code}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PlayListError", "加载默认列表失败: ${e.message}")
                    Toast.makeText(this@MusicPlayerActivity, "播放列表加载失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // 开始播放当前歌曲
    private fun startPlayCurrentSong() {
        if (currentPlayList.isNullOrEmpty() || currentIndex < 0 || currentIndex >= currentPlayList!!.size) {
            Log.e("PlayError", "无效的播放列表或索引")
            return
        }

        val currentSong = currentPlayList!![currentIndex]
        updateMusicInfo(
            songName = currentSong.name,
            coverUrl = currentSong.al.picUrl ?: "",
            singer = currentSong.ar.joinToString { it.name }
        )

        // 恢复当前歌曲的收藏状态
        restoreLikeState(currentSong.id.toString())

        // 同步列表到服务
        musicService?.currentPlayList = currentPlayList as List<ListMusicData.Song>
        musicService?.currentIndex = currentIndex

        // 更新列表显示
        updateFragmentPlayList()

        if (needApplyPlayProgress) {
            Log.d("MusicPlayer", "应用初始进度: $playProgress")
            lastPausedProgress = playProgress
        } else {
            lastPausedProgress = 0
        }

        fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
    }

    // 初始化中心图片旋转动画
    private fun initCenterImgAnimation() {
        centerRotationAnimator =
            ObjectAnimator.ofFloat(binding.mpCenter, ImageView.ROTATION, 0f, 360f).apply {
                duration = 20000  // 旋转一周的时间
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
    }

    // 进度事件发送器
    private fun initProgressEventSender() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isServiceReady && musicService != null) {
                    val currentSong = currentPlayList?.getOrNull(currentIndex)
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

    private fun initEventListeners() {
        // 返回按钮
        binding.mpMback.setOnClickListener {
            musicService?.lastSavedProgress = lastPausedProgress
            finish()
        }

        // 列表按钮 - 切换显示/隐藏
        binding.mpList.setOnClickListener {
            listFragment?.let {
                if (it.isAdded) {
                    if (supportFragmentManager.findFragmentById(binding.mpFragment.id)?.isHidden == true) {
                        showFragment()
                    } else {
                        hideFragment()
                    }
                } else {
                    Toast.makeText(this, "列表加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 收藏按钮
        binding.mpLike.setOnClickListener {
            val currentSongId = getCurrentSongId()
            if (currentSongId.isBlank()) {
                Toast.makeText(this, "无法获取歌曲ID，操作失败", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isLiked = !isLiked
            binding.mpLike.setImageResource(if (isLiked) R.drawable.like_last else R.drawable.firstlike)
            Toast.makeText(this, if (isLiked) "已收藏" else "已取消", Toast.LENGTH_SHORT).show()

            val sharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("liked_$currentSongId", isLiked)
                .apply()
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

    // 根据歌曲ID查找在当前列表中的索引
    private fun findSongIndexById(targetId: String): Int {
        if (targetId.isBlank() || currentPlayList.isNullOrEmpty()) return -1
        return try {
            val targetIdLong = targetId.toLong()
            currentPlayList?.indexOfFirst { it.id == targetIdLong } ?: -1
        } catch (e: NumberFormatException) {
            Log.e("MusicPlayer", "ID格式错误: $targetId")
            -1
        }
    }

    // 检查并播放当前歌曲
    fun playCurrentMusicWithCheck() {
        if (currentPlayList.isNullOrEmpty() || currentIndex < 0 || currentIndex >= currentPlayList!!.size) {
            Toast.makeText(this, "歌曲列表无效", Toast.LENGTH_SHORT).show()
            updatePlayButtonState(false)
            return
        }

        // 切换歌曲时重置旋转状态
        binding.mpRecord.animate().cancel()
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED

        // 重置中心图片旋转角度
        currentCenterRotation = 0f
        binding.mpCenter.rotation = 0f
        stopCenterImgAnimation()

        val currentSong = currentPlayList!![currentIndex]
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

        // 恢复当前歌曲的收藏状态
        restoreLikeState(currentSong.id.toString())

        // 同步到服务
        musicService?.currentPlayList = currentPlayList as List<ListMusicData.Song>
        musicService?.currentIndex = currentIndex

        // 更新列表Fragment
        updateFragmentPlayList()

        fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
    }

    private fun restoreLikeState(songId: String) {
        val sharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        isLiked = sharedPreferences.getBoolean("liked_$songId", false)
        binding.mpLike.setImageResource(if (isLiked) R.drawable.like_last else R.drawable.firstlike)
    }

    // 获取播放地址并播放
    private fun fetchAndPlayWithStateCheck(songId: String, songName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val musicUrlResponse = NetworkClient.apiService.getMusicUrl(songId)
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
        stopCenterImgAnimation()

        // 错误时重置旋转状态
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED
        currentCenterRotation = 0f
        binding.mpCenter.rotation = 0f
    }

    // 播放上一首
    private fun playLastSong() {
        EventBus.getDefault().post(CloseLyricEvent())
        EventBus.getDefault().post(CloseCommentEvent())
        if (currentPlayList.isNullOrEmpty()) {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show()
            return
        }

        // 切换前重置旋转状态
        binding.mpRecord.animate().cancel()
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED
        stopCenterImgAnimation()
        currentCenterRotation = 0f
        binding.mpCenter.rotation = 0f

        when (currentMode) {
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex - 1 + currentPlayList!!.size) % currentPlayList!!.size
            }

            MODE_SINGLE -> {
                musicService?.seekTo(0)
                lastPausedProgress = 0
                binding.mpSeekBar.progress = 0
                binding.mpTimestart.text = formatTime(0)
                return
            }

            MODE_RANDOM -> {
                if (currentPlayList!!.size <= 1) {
                    currentIndex = 0
                } else {
                    var randomIndex: Int
                    do {
                        randomIndex = Random.nextInt(currentPlayList!!.size)
                    } while (randomIndex == currentIndex)
                    currentIndex = randomIndex
                }
            }
        }

        // 同步到服务
        musicService?.currentIndex = currentIndex
        playCurrentMusicWithCheck()
    }

    // 播放下一首
    private fun playNextSong() {
        EventBus.getDefault().post(CloseLyricEvent())
        EventBus.getDefault().post(CloseCommentEvent())
        if (currentPlayList.isNullOrEmpty()) {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show()
            return
        }

        // 切换前重置旋转状态
        binding.mpRecord.animate().cancel()
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED
        stopCenterImgAnimation()
        currentCenterRotation = 0f
        binding.mpCenter.rotation = 0f

        when (currentMode) {
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex + 1) % currentPlayList!!.size
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
                if (currentPlayList!!.size <= 1) {
                    currentIndex = 0
                } else {
                    var randomIndex: Int
                    do {
                        randomIndex = Random.nextInt(currentPlayList!!.size)
                    } while (randomIndex == currentIndex)
                    currentIndex = randomIndex
                }
            }
        }

        // 同步到服务
        musicService?.currentIndex = currentIndex
        playCurrentMusicWithCheck()
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

    // 初始化进度条监听
    private fun initSeekBarListener() {
        binding.mpSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.mpTimestart.text = formatTime(progress)
                    if (isServiceReady) {
                        musicService?.seekTo(progress)
                    }
                    lastPausedProgress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isServiceReady && musicService?.isPlaying == true) {
                    handler.post(updateSeekBarRunnable)
                }
                seekBar?.progress?.let {
                    if (isServiceReady) {
                        musicService?.seekTo(it)
                    }
                }
            }
        })
    }

    // 初始化播放按钮
    private fun initPlayButton() {
        binding.mpStart.setOnClickListener {
            handlePlayClick()
        }
        updatePlayButtonState(false)
    }

    // 处理播放/暂停点击
    private fun handlePlayClick() {
        if (!isServiceBound) {
            val serviceIntent = Intent(this, MusicPlayService::class.java)
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
            Toast.makeText(this, "服务连接中...", Toast.LENGTH_SHORT).show()
            return
        }

        if (musicService == null) {
            Toast.makeText(this, "播放器未就绪", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val isPlaying = musicService?.isPlaying == true
            val newState = !isPlaying
            updatePlayButtonState(newState)

            if (isPlaying) {
                musicService?.pause()
                currentCenterRotation = binding.mpCenter.rotation
                stopCenterImgAnimation()
                if (currentRotation != ROTATION_PAUSED) {
                    rotateToPaused()
                }
            } else {
                if (musicService?.currentUrl.isNullOrBlank()) {
                    if (currentPlayList.isNullOrEmpty() || currentIndex < 0 || currentIndex >= currentPlayList!!.size) {
                        initCurrentPlayList()
                    } else {
                        val currentSong = currentPlayList!![currentIndex]
                        fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
                    }
                } else {
                    musicService?.resume()
                    startCenterImgAnimation(currentCenterRotation)
                    if (currentRotation != ROTATION_PLAYING) {
                        rotateToPlaying()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PlayLogic", "处理播放点击时发生异常", e)
            Toast.makeText(this, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            updatePlayButtonState(musicService?.isPlaying == true)
        }
    }

    // 更新播放按钮状态
    private fun updatePlayButtonState(isPlaying: Boolean) {
        runOnUiThread {
            val iconRes = if (isPlaying) R.drawable.mp_stop else R.drawable.mp_start
            binding.mpStart.setImageResource(iconRes)
            binding.mpStart.invalidate()
        }
    }

    // 旋转到播放状态
    private fun rotateToPlaying() {
        binding.mpRecord.post {
            binding.mpRecord.animate().cancel()
            val anim = binding.mpRecord.animate()
                .rotation(ROTATION_PLAYING)
                .setDuration(500)
                .setInterpolator(LinearInterpolator())

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
                .setInterpolator(LinearInterpolator())

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
            if (currentCenterRotation == 0f) {
                startCenterImgAnimation(0f)
            } else {
                startCenterImgAnimation(currentCenterRotation)
            }
            if (currentRotation != ROTATION_PLAYING) {
                rotateToPlaying()
            }
        } else {
            currentCenterRotation = binding.mpCenter.rotation
            stopCenterImgAnimation()
            if (currentRotation != ROTATION_PAUSED) {
                rotateToPaused()
            }
        }
    }

    // 启动中心图片动画
    private fun startCenterImgAnimation(startAngle: Float) {
        centerRotationAnimator?.cancel()
        binding.mpCenter.rotation = startAngle
        centerRotationAnimator = ObjectAnimator.ofFloat(
            binding.mpCenter,
            ImageView.ROTATION,
            startAngle,
            startAngle + 360f
        ).apply {
            duration = 20000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        centerRotationAnimator?.start()
    }

    // 停止中心图片动画
    private fun stopCenterImgAnimation() {
        centerRotationAnimator?.cancel()
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
                updatePlayButtonState(isPlaying)
                updateProgressUI(lastPausedProgress, totalDuration)
                updateAnimationState(isPlaying)
            }
        }

        try {
            val currentSong = currentPlayList?.getOrNull(currentIndex)
                ?: throw Exception("当前歌曲不存在于列表中")

            binding.mpRecord.rotation = ROTATION_PAUSED
            currentRotation = ROTATION_PAUSED

            musicService?.play(
                url,
                song = currentSong
            )

            currentCenterRotation = 0f
            startCenterImgAnimation(0f)
            rotateToPlaying()
            onPrepared(true)
        } catch (e: Exception) {
            Log.e("AudioError", "播放异常: ${e.message}")
            onPrepared(false)
        }
    }

    // 获取当前歌曲ID
    private fun getCurrentSongId(): String {
        return if (currentIndex in currentPlayList?.indices ?: emptyList()) {
            currentPlayList!![currentIndex].id.toString()
        } else {
            id ?: ""
        }
    }

    // 更新进度UI
    private fun updateProgressUI(currentProgress: Int, totalDuration: Int) {
        binding.mpSeekBar.max = totalDuration
        binding.mpSeekBar.progress = currentProgress
        binding.mpTimestart.text = formatTime(currentProgress)
        binding.mpTimend.text = formatTime(totalDuration)
    }

    // 格式化时间
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

                if (totalDuration > 0 && lastPausedProgress >= (totalDuration - 5000) && !isPreparePlayNext) {
                    playNextSong()
                }
            }
            handler.postDelayed(this, 300)
        }
    }

    // 服务状态同步
    private fun syncServiceStateAfterConnection() {
        if (musicService == null) return

        val serviceIsPlaying = musicService?.isPlaying == true
        updatePlayButtonState(serviceIsPlaying)

        // 同步进度
        lastPausedProgress = musicService?.lastSavedProgress ?: 0
        totalDuration = musicService?.getDuration() ?: 0
        updateProgressUI(lastPausedProgress, totalDuration)

        // 同步动画状态
        updateAnimationState(serviceIsPlaying)
    }

    // 设置服务监听器
    private fun setupServiceListeners() {
        // 监听播放状态变化
        musicService?.setOnPlayStateChanged { isPlaying, duration ->
            runOnUiThread {
                totalDuration = duration
                updatePlayButtonState(isPlaying)
                updateProgressUI(lastPausedProgress, totalDuration)
                updateAnimationState(isPlaying)
            }
        }

        // 监听进度变化
        musicService?.currentProgress?.observe(this) { progress ->
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
            if (musicService != null) {
                try {
                    musicService?.pause()
                } catch (e: Exception) {
                    Log.e("MusicPlayer", "暂停音乐失败", e)
                }
            }
            val targetSingerId = if (currentIndex in currentPlayList?.indices ?: emptyList()) {
                currentPlayList!![currentIndex].ar.firstOrNull()?.id ?: 0L
            } else {
                singerId?.toLong() ?: 0L
            }

            if (targetSingerId <= 0) {
                val errorMsg = "无法获取有效歌手ID"
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("MusicPlayer", errorMsg)
                return@setOnClickListener
            }

            TheRouter.build("/singer/SingerActivity")
                .withLong("id", targetSingerId)
                .navigation()
        }

        // 评论点击
        binding.mpCommon.setOnClickListener {
            val currentSongId = getCurrentSongId()
            if (currentSongId.isBlank()) {
                Toast.makeText(this, "无法获取歌曲ID，无法查看评论", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            TheRouter.build("/module_musicplayer/commentActivity")
                .withString("id", currentSongId)
                .navigation()
        }

        // 歌词点击
        binding.mpCenter.setOnClickListener {
            val currentSongId = getCurrentSongId()
            if (currentSongId.isBlank()) {
                Toast.makeText(this, "无法获取歌曲ID，无法查看歌词", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        if (isServiceReady && musicService != null) {
            // 从服务同步最新的列表和索引
            currentPlayList = musicService?.currentPlayList
            currentIndex = musicService?.currentIndex ?: currentIndex
            updateFragmentPlayList()

            val currentIsPlaying = musicService?.isPlaying == true
            updatePlayButtonState(currentIsPlaying)
            updateAnimationState(currentIsPlaying)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.lastSavedProgress = lastPausedProgress

        handler.removeCallbacksAndMessages(null)
        stopCenterImgAnimation()

        musicService?.setOnCompletionListener(null)
        musicService?.setOnPlayStateChanged(null)

        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}