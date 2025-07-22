package com.example.module_musicplayer

import SongChangeEvent
import android.animation.Animator


import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
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
import Adapter.MusicDataCache
import PlayProgressEvent
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

    private var isPlaying = false
    private lateinit var binding: MusicPlayerBinding
    private var fragment: Fragment? = null
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 90f
    private var currentRotation = ROTATION_PAUSED
    private var animation: Animation? = null
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())
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

    // 服务连接的回调
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayService.MusicBinder
            musicService = binder.service
            isServiceBound = true
            isServiceReady = true

            // 优先应用playProgress，避免被覆盖
            if (needApplyPlayProgress && playProgress > 0) {
                Log.d("MusicPlayer", "服务连接后应用进度: $playProgress")
                musicService?.seekTo(playProgress)
                binding.mpSeekBar.progress = playProgress
                binding.mpTimestart.text = formatTime(playProgress)
                needApplyPlayProgress = false
            }

            syncServiceState()

            musicService?.setOnCompletionListener {
                runOnUiThread {
                    playNextSong()
                }
            }

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

                Log.d("MusicPlayerActivity", "转发的歌手ID: $targetSingerId")
                TheRouter.build("/singer/SingerActivity")
                    .withLong("id", targetSingerId)
                    .navigation()
            }

            // 评论区跳转
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
                val currentPos = musicService?.getCurrentPosition() ?: 0
                val duration = musicService?.getDuration() ?: 0
                val route = TheRouter.build("/song/SongWord")
                    .withString("id", currentSongId)
                    .withInt("currentSongDuration", duration)
                    .withInt("currentPos", currentPos)


                route.navigation()
            }

            // 执行等待中的播放任务
            pendingPlayTask?.invoke()
            pendingPlayTask = null
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            isServiceReady = false
        }
    }

    // 进度条更新任务
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isServiceReady && musicService?.isPlaying == true) {
                val currentPosition = musicService?.getCurrentPosition() ?: 0
                val duration = musicService?.getDuration() ?: 0
                val currentSongId = if (currentIndex in musicList?.indices ?: emptyList()) {
                    musicList!![currentIndex].id.toString()
                } else {
                    id ?: ""
                }
                binding.mpSeekBar.progress = currentPosition
                binding.mpTimestart.text = formatTime(currentPosition)

                EventBus.getDefault().post(
                    PlayProgressEvent(
                        position = currentPosition,
                        duration = duration,
                        songId = id ?: ""
                    )
                )

                progressListener?.onProgressChanged(
                    songId = currentSongId,
                    duration = musicService?.getDuration() ?: 0,
                    position = currentPosition
                )
            }
            handler.postDelayed(this, 100)  // 每100ms更新一次
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mpName.requestFocus()

        // 注入路由参数
        TheRouter.inject(this)
        Log.d("MusicPlayerActivity", "接收的歌手ID: $singerId")
        // 是否需要应用底部栏传递的进度
        needApplyPlayProgress = playProgress > 0
        Log.d(
            "MusicPlayer",
            "初始化参数: 进度=$playProgress，是否应用=$needApplyPlayProgress，索引=$currentPosition"
        )

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

        // 初始化歌曲信息和列表
        initMusicInfo()
        initFragment()
        fetchMusic()

        initLikeStatus()

        // 启动进度更新
        handler.post(updateSeekBarRunnable)
    }

    // 获取音频文件URI（用于分享）
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

    // 同步服务状态到UI
    private fun syncServiceState() {
        if (isServiceReady && musicService != null) {
            val serviceIsPlaying = musicService?.isPlaying == true
            val serviceDuration = musicService?.getDuration() ?: 0
            // 优先使用未应用的进度，否则使用服务当前进度
            val servicePosition =
                if (needApplyPlayProgress) playProgress else musicService?.getCurrentPosition() ?: 0

            // 更新播放状态
            isPlaying = serviceIsPlaying
            binding.mpStart.setImageResource(if (serviceIsPlaying) R.drawable.mp_stop else R.drawable.mp_start)

            // 更新进度条
            binding.mpSeekBar.max = serviceDuration
            binding.mpSeekBar.progress = servicePosition
            binding.mpTimestart.text = formatTime(servicePosition)
            binding.mpTimend.text = formatTime(serviceDuration)

            // 更新动画状态
            if (serviceIsPlaying) {
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
            TheRouter.build("/main/main").navigation()
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
            saveLikeState(isLiked)
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

    private fun saveLikeState(isLiked: Boolean) {
        val currentSongId=if(musicList?.indices?.contains(currentIndex) == true) {
            musicList!![currentIndex].id
        }else {
            id
        }
        val prefs = getSharedPreferences("like_state", MODE_PRIVATE)
        prefs.edit().putBoolean(currentSongId.toString(), isLiked).apply()
        Log.d("LikeStatus", "歌曲 $currentSongId 收藏状态：$isLiked")
    }
    private fun initLikeStatus() {
        val currentSongId = if (currentIndex in musicList?.indices ?: emptyList()) {
            musicList!![currentIndex].id.toString()
        } else {
            id ?: return
        }

        val prefs = getSharedPreferences("like_state", MODE_PRIVATE)
        isLiked = prefs.getBoolean(currentSongId, false)
        binding.mpLike.setImageResource(if (isLiked) R.drawable.like_last else R.drawable.like)
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

            // 重置进度（切换歌曲时）
            musicService?.stop()
            binding.mpSeekBar.progress = 0
            binding.mpTimestart.text = formatTime(0)
            currentMusic = null

            fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
        } else {
            Toast.makeText(this, "歌曲不存在", Toast.LENGTH_SHORT).show()
            binding.mpStart.setImageResource(R.drawable.mp_start)
        }
    }

    // 获取播放地址并播放（带状态检查）
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
                                    binding.mpStart.setImageResource(if (success) R.drawable.mp_stop else R.drawable.mp_start)
                                }
                            } else {
                                pendingPlayTask = {
                                    playAudio(musicUrl) { success ->
                                        binding.mpStart.isEnabled = true
                                        binding.mpStart.setImageResource(if (success) R.drawable.mp_stop else R.drawable.mp_start)
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
        binding.mpStart.setImageResource(R.drawable.mp_start)
        musicService?.stop()
        isPlaying = false
        stopCenterImg()
        if (currentRotation != ROTATION_PAUSED) {
            rotateToPaused()
        }
    }

    // 播放上一首
    private fun playLastSong() {
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
        if (musicList?.indices?.contains(currentIndex) == true) {
            val newSong = musicList!![currentIndex]
            // 发送歌曲切换事件
            EventBus.getDefault().post(
                SongChangeEvent(
                    newSongId = newSong.id.toString(),
                    initialPosition = 0
                )
            )

            playCurrentMusicWithCheck()
            initLikeStatus()
        }
    }

    // 播放下一首
    private fun playNextSong() {
        if (musicList.isNullOrEmpty()) {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex + 1) % musicList!!.size
            }

            MODE_SINGLE -> {
                playCurrentMusicWithCheck()
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
        if (musicList?.indices?.contains(currentIndex) == true) {
            val newSong = musicList!![currentIndex]
            // 发送歌曲切换事件
            EventBus.getDefault().post(
                SongChangeEvent(
                    newSongId = newSong.id.toString(),
                    initialPosition = 0 // 新歌曲从0开始播放
                )
            )
            // 继续原有播放逻辑
            playCurrentMusicWithCheck()
            initLikeStatus()
        }
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
                // 更新歌曲信息UI
                updateMusicInfo(
                    songName = currentSong.name,
                    coverUrl = currentSong.al.picUrl ?: "",
                    singer = currentSong.ar.joinToString { it.name }
                )
                // 不重新加载，等待服务连接后应用进度
                Log.d("MusicPlayer", "等待服务连接后应用进度: $playProgress")
            } else {
                // 不需要应用进度时，正常加载歌曲
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
                        musicList = dailySongsResponse.data?.dailySongs as? List<ListMusicData.Song>?
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
                } catch (e: Exception){
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

    // 格式化时间（毫秒转分:秒）
    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // 初始化进度条监听
    private fun initSeekBarListener() {
        binding.mpSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isServiceReady) {
                    musicService?.seekTo(progress)
                    binding.mpTimestart.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateSeekBarRunnable)
            }
        })
    }

    // 初始化播放按钮
    private fun initPlayButton() {
        binding.mpStart.setImageResource(R.drawable.mp_start)
        binding.mpStart.setOnClickListener {
            if (!isServiceReady) {
                Toast.makeText(this, "服务初始化中，请稍候...", Toast.LENGTH_SHORT).show()
                pendingPlayTask = ::handlePlayClick
                return@setOnClickListener
            }
            handlePlayClick()
        }
    }

    // 处理播放/暂停点击
    private fun handlePlayClick() {
        if (!isServiceBound || musicService == null) {
            Toast.makeText(this, "服务连接失败", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPlaying) {
            musicService?.pause()
            isPlaying = false
            binding.mpStart.setImageResource(R.drawable.mp_start)
            rotateToPaused()
            stopCenterImg()
        } else {
            binding.mpStart.isEnabled = false
            binding.mpStart.setImageResource(R.drawable.mp_stop)

            if (currentMusic.isNullOrBlank()) {
                if (currentIndex in musicList?.indices ?: emptyList()) {
                    val currentSong = musicList!![currentIndex]
                    fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
                } else {
                    Toast.makeText(this, "无可用歌曲", Toast.LENGTH_SHORT).show()
                    binding.mpStart.isEnabled = true
                    binding.mpStart.setImageResource(R.drawable.mp_start)
                }
            } else {
                try {
                    val currentPos = musicService?.getCurrentPosition() ?: 0
                    val currentSong = if (currentIndex in musicList?.indices ?: emptyList()) {
                        musicList!![currentIndex]
                    } else {
                        null
                    }

                    if (currentPos > 0 && currentPos < (musicService?.getDuration() ?: 0)) {
                        musicService?.resume()
                    } else {
                        if (currentSong != null) {
                            musicService?.play(currentMusic!!, currentSong)
                        } else {
                            throw Exception("无法获取歌曲信息")
                        }
                    }
                    isPlaying = true
                    handler.post(updateSeekBarRunnable)
                    binding.mpStart.isEnabled = true
                    rotateToPlaying()
                    startCenterImg()
                } catch (e: Exception) {
                    Log.e("PlayError", "播放失败: ${e.message}")
                    Toast.makeText(this, "播放失败，请重试", Toast.LENGTH_SHORT).show()
                    isPlaying = false
                    binding.mpStart.isEnabled = true
                    binding.mpStart.setImageResource(R.drawable.mp_start)
                    rotateToPaused()
                }
            }
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

        musicService?.setOnPlayStateChanged { serviceIsPlaying, duration ->
            runOnUiThread {
                isPlaying = serviceIsPlaying
                binding.mpSeekBar.max = duration
                binding.mpTimend.text = formatTime(duration)

                if (serviceIsPlaying) {
                    startCenterImg()
                    rotateToPlaying()
                    onPrepared(true)
                } else {
                    stopCenterImg()
                    rotateToPaused()
                    onPrepared(false)
                }
            }
        }

        try {
            val currentSong = musicList?.getOrNull(currentIndex)
                ?: throw Exception("当前歌曲不存在于列表中")

            musicService?.play(
                url,
                song = currentSong
            )
            Log.d("FixSuccess", "传递歌曲信息: ${currentSong.name}")
        } catch (e: Exception) {
            Log.e("AudioError", "播放异常: ${e.message}")
            onPrepared(false)
        }
    }


    interface onPlayProgressListener {
        fun onProgressChanged(position:Int,duration: Int,songId: String)
    }

    private var progressListener:onPlayProgressListener? = null
    // 销毁时清理资源

    fun setOnPLayProgressListener(listener:onPlayProgressListener){
        progressListener = listener
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        stopCenterImg()
    }
}
