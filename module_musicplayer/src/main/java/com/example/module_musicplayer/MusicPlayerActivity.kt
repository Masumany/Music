package com.example.module_musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.lib.base.NetWorkClient
import com.example.lib.base.Song
import com.example.module_musicplayer.databinding.MusicPlayerBinding
import com.example.module_recommened.RecommendActivity
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

@Route(path = "/module_musicplayer/musicplayer")
class MusicPlayerActivity : AppCompatActivity() {

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


    private var isPlaying = false
    private lateinit var binding: MusicPlayerBinding
    private var fragment: Fragment? = null
    // 定义两个固定旋转位置：0°（暂停）和90°（播放）
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 90f
    private var currentRotation = ROTATION_PAUSED // 当前旋转角度
    private var animation: Animation? = null
    private var isLiked = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var currentMusic: String? = null
    private var musicList: List<Song>? = null
    private var currentIndex = -1

    // 修正播放模式常量定义
    private val MODE_SEQUENTIAL = 0  // 顺序播放
    private val MODE_SINGLE = 1      // 单曲循环
    private val MODE_RANDOM = 2      // 随机播放
    private var currentMode = MODE_SEQUENTIAL  // 默认顺序播放

    // 服务相关
    private var musicService: MusicPlayService? = null
    private var isServiceBound = false
    private var isServiceReady = false
    private var pendingPlayTask: (() -> Unit)? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicPlayService.MusicBinder
            musicService = binder.service
            isServiceBound = true
            isServiceReady = true

            // 同步服务状态到UI
            syncServiceState()

            // 关键：设置播放完成监听，实现自动播放下一首
            musicService?.setOnCompletionListener {
                runOnUiThread {
                    // 播放完成后自动调用下一首逻辑
                    playNextSong()
                }
            }

            binding.mpCommon.setOnClickListener {
                val currentSongId = if (currentIndex in musicList?.indices ?: emptyList()) {
                    musicList!![currentIndex].id.toString()
                } else {
                    // 2. 备选方案：使用初始化时的id参数
                    id ?: ""
                }

                // 3. 检查id是否有效
                if (currentSongId.isBlank()) {
                    Toast.makeText(this@MusicPlayerActivity, "无法获取歌曲ID，无法查看评论", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 4. 传递有效的id到CommentActivity
                val router = TheRouter.build("/module_musicplayer/commentActivity")
                    .withString("id", currentSongId) // 确保id不为空
                router.navigation()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            isServiceReady = false
        }
    }

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isServiceReady && musicService?.isPlaying == true) {
                val currentPosition = musicService?.getCurrentPosition() ?: 0
                binding.mpSeekBar.progress = currentPosition
                binding.mpTimestart.text = formatTime(currentPosition)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mpName.requestFocus()

        TheRouter.inject(this)
        Log.d("MusicPlayer", "初始化参数: id=$id, cover=$cover, songListName=$songListName")

        // 从SharedPreferences恢复播放模式
        restorePlayMode()

        // 初始化旋转控件位置为暂停状态（0°）
        binding.mpRecord.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED

        // 先启动服务再绑定
        val serviceIntent = Intent(this, MusicPlayService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        binding.mpCount.setOnClickListener{
            changeMode()
        }
        // 在MusicPlayerActivity中添加音频分享功能
        binding.mpShare.setOnClickListener {
            val audioUri = getCurrentAudioUri()
            if (audioUri == null) {
                Toast.makeText(this, "无法获取音频文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*" // 音频类型的MIME标识
                putExtra(Intent.EXTRA_STREAM, audioUri)
                putExtra(Intent.EXTRA_TEXT, "推荐一首好歌：${binding.mpName.text}")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(shareIntent, "分享音频到"))
        }




        // 初始化UI
        initMusicInfo()

        // 初始化Fragment
        initFragment()

        fetchMusic()
        initEventListeners()
        initCenterImg()
        initSeekBarListener()
        initPlayButton()
        initNextSong()
        initLastSong()

        // 初始化模式图标
        updateModeIcon()

        handler.post(updateSeekBarRunnable)
    }
    // 获取当前播放音频的Content URI
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
    // 恢复保存的播放模式
    private fun restorePlayMode() {
        val prefs: SharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        currentMode = prefs.getInt("play_mode", MODE_SEQUENTIAL)
    }

    // 保存播放模式
    private fun savePlayMode() {
        val prefs: SharedPreferences = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
        prefs.edit().putInt("play_mode", currentMode).apply()
    }

    // 更新模式图标
    private fun updateModeIcon() {
        val iconRes = when (currentMode) {
            MODE_SEQUENTIAL -> R.drawable.play_count  // 顺序播放图标
            MODE_SINGLE -> R.drawable.mp_one          // 单曲循环图标
            MODE_RANDOM -> R.drawable.mp_change       // 随机播放图标
            else -> R.drawable.play_count
        }
        binding.mpCount.setImageResource(iconRes)
    }

    private fun changeMode() {
        // 切换模式（顺序→单曲→随机循环）
        currentMode = (currentMode + 1) % 3

        // 更新图标
        updateModeIcon()

        // 保存模式
        savePlayMode()

        // 显示切换提示
        val modeName = when (currentMode) {
            MODE_SEQUENTIAL -> "顺序播放"
            MODE_SINGLE -> "单曲循环"
            MODE_RANDOM -> "随机播放"
            else -> "顺序播放"
        }
        Toast.makeText(this, "已切换为$modeName", Toast.LENGTH_SHORT).show()
    }

    private fun initMusicInfo() {
        song?.let {
            updateMusicInfo(it.name, it.al.picUrl ?: "", it.ar.joinToString { a -> a.name })
        } ?: run {
            val safeSongName = songListName ?: "未知歌曲"
            val safeCover = cover ?: ""
            updateMusicInfo(safeSongName, safeCover)
        }
    }

    private fun initFragment() {
        fragment = supportFragmentManager.findFragmentById(binding.mpFragment.id)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().hide(fragment!!).commit()
        } else {
            Log.w("MusicPlayer", "Fragment容器为空")
        }
    }

    private fun syncServiceState() {
        if (isServiceReady && musicService != null) {
            val serviceIsPlaying = musicService?.isPlaying == true
            val serviceDuration = musicService?.getDuration() ?: 0
            val servicePosition = musicService?.getCurrentPosition() ?: 0

            // 同步状态变量
            isPlaying = serviceIsPlaying

            // 同步UI图标
            binding.mpStart.setImageResource(if (serviceIsPlaying) R.drawable.mp_stop else R.drawable.mp_start)

            // 同步进度条
            binding.mpSeekBar.max = serviceDuration
            binding.mpSeekBar.progress = servicePosition
            binding.mpTimestart.text = formatTime(servicePosition)
            binding.mpTimend.text = formatTime(serviceDuration)

            // 同步旋转状态（确保与服务状态一致）
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

    private fun initLastSong() {
        binding.mpLast.setOnClickListener {
            playLastSong()
        }
    }

    private fun initNextSong() {
        binding.mpNext.setOnClickListener {
            playNextSong()
        }
    }

    private fun initEventListeners() {
        binding.mpMback.setOnClickListener {
           val router= TheRouter.build("/main/main")
            router.navigation()
            finish()
        }

        binding.mpList.setOnClickListener {
            fragment?.let { changeFragment() }
                ?: Toast.makeText(this, "列表加载失败", Toast.LENGTH_SHORT).show()
        }

        binding.mpLike.setOnClickListener {
            isLiked = !isLiked
            binding.mpLike.setImageResource(if (isLiked) R.drawable.like_last else R.drawable.like)
            Toast.makeText(this, if (isLiked) "已收藏" else "已取消", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findSongIndexById(targetId: String): Int {
        if (targetId.isBlank() || musicList.isNullOrEmpty())
            return -1
        return try {
            val targetIdLong = targetId.toLong()
            musicList?.indexOfFirst { it.id == targetIdLong } ?: -1
        } catch (e: NumberFormatException) {
            Log.e("MusicPlayer", "ID格式错误: $targetId")
            -1
        }
    }


    // 带状态检查的播放方法
    private fun playCurrentMusicWithCheck() {
        if (musicList?.indices?.contains(currentIndex) == true) {
            val currentSong = musicList!![currentIndex]
            updateMusicInfo(
                songName = currentSong.name,
                coverUrl = currentSong.al.picUrl ?: "",
                singer = currentSong.ar.joinToString { it.name }
            )

            // 关键：强制停止当前播放，重置进度条（确保单曲循环从头开始）
            musicService?.stop()
            binding.mpSeekBar.progress = 0
            binding.mpTimestart.text = formatTime(0)
            currentMusic = null // 清空当前音乐地址，强制重新加载

            // 重新请求播放当前歌曲
            fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
        } else {
            Toast.makeText(this, "歌曲不存在", Toast.LENGTH_SHORT).show()
            binding.mpStart.setImageResource(R.drawable.mp_start)
        }
    }

    // 带状态检查的播放请求方法
    private fun fetchAndPlayWithStateCheck(songId: String, songName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val musicUrlResponse = NetWorkClient.apiService3.getMusicUrl(songId)
                withContext(Dispatchers.Main) {
                    if (musicUrlResponse.code == 200 && musicUrlResponse.data != null && musicUrlResponse.data.isNotEmpty()) {
                        val musicUrl = musicUrlResponse.data[0].url
                        if (!musicUrl.isNullOrBlank()) {
                            // 歌曲获取正常：尝试播放并准备显示stop图标
                            currentMusic = musicUrl
                            if (isServiceReady) {
                                playAudio(musicUrl) { success ->
                                    binding.mpStart.isEnabled = true
                                    // 根据播放结果设置图标
                                    binding.mpStart.setImageResource(if (success) R.drawable.mp_stop else R.drawable.mp_start)
                                }
                            } else {
                                pendingPlayTask = {
                                    playAudio(musicUrl) { success ->
                                        binding.mpStart.isEnabled = true
                                        binding.mpStart.setImageResource(if (success) R.drawable.mp_stop else R.drawable.mp_start)
                                    }
                                }
                                Toast.makeText(this@MusicPlayerActivity, "服务初始化中，请稍候...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 歌曲无播放地址：显示start图标
                            handleSongError(songName)
                        }
                    } else {
                        // 歌曲获取失败：显示start图标
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

    // 歌曲获取失败的统一处理方法
    private fun handleSongError(songName: String) {
        Toast.makeText(this, "歌曲《$songName》无法播放", Toast.LENGTH_SHORT).show()
        // 显示start图标
        binding.mpStart.setImageResource(R.drawable.mp_start)
        // 停止播放状态
        musicService?.stop()
        isPlaying = false
        stopCenterImg()
        // 确保旋转位置为暂停状态
        if (currentRotation != ROTATION_PAUSED) {
            rotateToPaused()
        }
    }

    private fun playLastSong() {
        if (musicList.isNullOrEmpty()) {
            Toast.makeText(this, "没有上一首", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            // 顺序播放：索引-1，小于0则跳到最后一首
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex - 1 + musicList!!.size) % musicList!!.size
            }

            // 单曲循环：保持当前索引
            MODE_SINGLE -> {
                musicService?.seekTo(0)
            }

            // 随机播放：生成不同的随机索引
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

    private fun playNextSong() {
        if (musicList.isNullOrEmpty()) {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            // 1. 顺序播放：当前索引+1，循环到第一首
            MODE_SEQUENTIAL -> {
                currentIndex = (currentIndex + 1) % musicList!!.size
            }

            // 2. 单曲循环：保持当前索引不变
            MODE_SINGLE -> {
                playCurrentMusicWithCheck()
            }

            // 3. 随机播放：生成不与当前索引重复的随机索引
            MODE_RANDOM -> {
                if (musicList!!.size <= 1) {
                    currentIndex = 0
                } else {
                    var randomIndex: Int
                    do {
                        randomIndex = Random.nextInt(musicList!!.size)
                    } while (randomIndex == currentIndex)  // 确保不重复当前歌曲
                    currentIndex = randomIndex
                }
            }
        }

        // 播放计算后的下一首
        playCurrentMusicWithCheck()
    }

    private fun fetchMusic() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dailySongsResponse = NetWorkClient.apiService2.getListData()
                if (dailySongsResponse.code == 200 && !dailySongsResponse.data?.dailySongs.isNullOrEmpty()) {
                    musicList = dailySongsResponse.data?.dailySongs
                    withContext(Dispatchers.Main) {
                        currentIndex = findSongIndexById(id ?: "")
                        if (currentIndex == -1) {
                            currentIndex = 0
                        }
                        fetchAndPlayWithStateCheck(musicList!![currentIndex].id.toString(), musicList!![currentIndex].name)
                    }
                } else {
                    throw Exception("获取歌曲列表失败")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MusicPlayerActivity, "获取歌曲列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.mpStart.setImageResource(R.drawable.mp_start)
                }
            }
        }
    }

    private fun updateMusicInfo(songName: String, coverUrl: String, singer: String? = null) {
        binding.mpName.text = songName
        binding.mpSinger.text = singer ?: athour ?: "未知歌手"
        Glide.with(this)
            .load(coverUrl)
            .error(R.drawable.ic_launcher_background)
            .into(binding.mpCenter)
    }

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

    fun hideFragment() {
        fragment?.let {
            if (it.isVisible) {
                supportFragmentManager.beginTransaction().hide(it).commit()
            }
        }
    }

    private fun initCenterImg() {
        animation = AnimationUtils.loadAnimation(this, R.anim.img_animation).apply {
            interpolator = LinearInterpolator()
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

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

    private fun initPlayButton() {
        // 初始状态设为未播放
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

    private fun handlePlayClick() {
        if (!isServiceBound || musicService == null) {
            Toast.makeText(this, "服务连接失败", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPlaying) {
            // 暂停播放
            musicService?.pause()
            isPlaying = false
            binding.mpStart.setImageResource(R.drawable.mp_start)
            rotateToPaused() // 旋转到暂停位置（0°）
            stopCenterImg()
        } else {
            // 播放/恢复播放
            binding.mpStart.isEnabled = false
            binding.mpStart.setImageResource(R.drawable.mp_stop)

            if (currentMusic.isNullOrBlank()) {
                // 无播放地址时重新加载
                if (currentIndex in musicList?.indices ?: emptyList()) {
                    val currentSong = musicList!![currentIndex]
                    fetchAndPlayWithStateCheck(currentSong.id.toString(), currentSong.name)
                } else {
                    Toast.makeText(this, "无可用歌曲", Toast.LENGTH_SHORT).show()
                    binding.mpStart.isEnabled = true
                    binding.mpStart.setImageResource(R.drawable.mp_start)
                }
            } else {
                // 有播放地址时恢复播放
                try {
                    val currentPos = musicService?.getCurrentPosition() ?: 0
                    if (currentPos > 0 && currentPos < (musicService?.getDuration() ?: 0)) {
                        musicService?.resume()
                    } else {
                        musicService?.play(currentMusic!!)
                    }
                    isPlaying = true
                    handler.post(updateSeekBarRunnable)
                    binding.mpStart.isEnabled = true
                    rotateToPlaying() // 旋转到播放位置（90°）
                    startCenterImg()
                } catch (e: Exception) {
                    Log.e("PlayError", "播放失败: ${e.message}")
                    Toast.makeText(this, "播放失败，请重试", Toast.LENGTH_SHORT).show()
                    isPlaying = false
                    binding.mpStart.isEnabled = true
                    binding.mpStart.setImageResource(R.drawable.mp_start)
                    rotateToPaused() // 失败时旋转到暂停位置
                }
            }
        }
    }

    // 旋转到播放位置（90°）
    private fun rotateToPlaying() {
        binding.mpRecord.post {
            binding.mpRecord.animate().cancel()
            val anim = binding.mpRecord.animate()
                .rotation(ROTATION_PLAYING)
                .setDuration(500)

            anim.interpolator = LinearInterpolator()

            anim.setListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    currentRotation = ROTATION_PLAYING
                    anim.setListener(null)
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {}

                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            anim.start()
        }
    }

    // 旋转到暂停位置（0°）
    private fun rotateToPaused() {
        binding.mpRecord.post {
            binding.mpRecord.animate().cancel()
            val anim = binding.mpRecord.animate()
                .rotation(ROTATION_PAUSED)
                .setDuration(500)

            anim.interpolator = LinearInterpolator()

            anim.setListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    currentRotation = ROTATION_PAUSED
                    anim.setListener(null)
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {}

                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            anim.start()
        }
    }


    private fun startCenterImg() {
        animation?.let {
            binding.mpCenter.startAnimation(it)
        }
    }

    private fun stopCenterImg() {
        animation?.let {
            binding.mpCenter.clearAnimation()
        }
    }

    private fun playAudio(url: String, onPrepared: (Boolean) -> Unit) {
        if (!isServiceReady || musicService == null) {
            onPrepared(false)
            return
        }

        // 清除之前的回调
        musicService?.setOnPlayStateChanged(null)
        // 关键：确保播放完成监听已绑定（与serviceConnection中设置的一致）
        musicService?.setOnCompletionListener {
            runOnUiThread {
                playNextSong()
            }
        }

        // 设置新的播放状态回调（原有逻辑不变）
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
            musicService?.play(url)
        } catch (e: Exception) {
            Log.e("AudioError", "播放异常: ${e.message}")
            onPrepared(false)
        }
    }

    private fun pauseAudio() {
        musicService?.pause()
        isPlaying = false
        handler.removeCallbacks(updateSeekBarRunnable)
        stopCenterImg()
        rotateToPaused()
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
