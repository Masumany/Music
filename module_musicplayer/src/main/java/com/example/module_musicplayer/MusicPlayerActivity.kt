package com.example.module_musicplayer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
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
import com.example.lib.base.Song  // 导入Song类
import com.example.module_musicplayer.databinding.MusicPlayerBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var binding: MusicPlayerBinding
    private var fragment: Fragment? = null
    private var currentRotation = 0f
    private var animation: Animation? = null
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())
    private var currentMusic: String? = null
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    val currentPosition = it.currentPosition
                    binding.mpSeekBar.progress = currentPosition
                    binding.mpTimestart.text = formatTime(currentPosition)
                }
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
        Log.d("MusicPlayer", "初始化参数: id=$id, cover=$cover, songName=$songName, song=$song")

        // 初始化UI - 处理空值
        song?.let {
            it.al.picUrl?.let { it1 -> updateMusicInfo(it.name, it1) }
        } ?: run {
            val safeSongName = songName ?: "未知歌曲"
            val safeCover = cover ?: ""
            updateMusicInfo(safeSongName, safeCover)
        }

        // 初始化Fragment - 增加空判断
        fragment = supportFragmentManager.findFragmentById(binding.mpFragment.id)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().hide(fragment!!).commit()
        } else {
            Log.w("MusicPlayer", "Fragment容器中未找到Fragment实例")
        }

        fetchMusic()
        initEventListeners()
        initCenterImg()
        initSeekBarListener()
        initPlayButton()

    }

    private fun initEventListeners() {
        binding.mpMback.setOnClickListener {
            val intent = Intent(this, com.example.module_recommened.RecommendActivity::class.java)
            startActivity( intent)
        }

        binding.mpList.setOnClickListener {
            if (fragment != null) {
                changeFragment()
            } else {
                Toast.makeText(this, "列表加载失败", Toast.LENGTH_SHORT).show()
            }
        }

        // 收藏按钮
        binding.mpLike.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                binding.mpLike.setImageResource(R.drawable.like_last)
                Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show()
            } else {
                binding.mpLike.setImageResource(R.drawable.like)
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMusic() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 验证ID有效性
                val targetId = id ?: throw Exception("未获取到有效的歌曲ID")
                Log.d("MusicPlayer", "准备获取音乐地址，目标ID: $targetId")

                // 2. 首次请求
                var musicUrlResponse = NetWorkClient.apiService3.getMusicUrl(targetId)
                Log.d("MusicPlayer", "首次请求音乐地址响应: code=${musicUrlResponse.code}")

                // 3. 验证响应有效性（处理空值）
                var isResponseValid = false
                if (musicUrlResponse.code == 200 && !musicUrlResponse.data.isNullOrEmpty()) {
                    val url = musicUrlResponse.data[0].url
                    isResponseValid = !url.isNullOrBlank()
                }

                // 4. 备用方案
                if (!isResponseValid) {
                    Log.w("MusicPlayer", "首次请求失败，尝试推荐歌单获取")
                    val recommendResponse = NetWorkClient.apiService1.getRecommended()
                    if (recommendResponse.code != 200 || recommendResponse.result.isNullOrEmpty()) {
                        throw Exception("推荐数据获取失败")
                    }

                    val firstPlaylist = recommendResponse.result!![0]
                    val playListResponse = NetWorkClient.apiService4.getPlayList(firstPlaylist.id)
                    if (playListResponse.code != 200 || playListResponse.songs.isNullOrEmpty()) {
                        throw Exception("歌单详情获取失败")
                    }

                    val targetSong = playListResponse.songs[0]
                    musicUrlResponse = NetWorkClient.apiService3.getMusicUrl(targetSong.id.toString())

                    // 再次验证
                    if (musicUrlResponse.code == 200 && !musicUrlResponse.data.isNullOrEmpty()) {
                        val url = musicUrlResponse.data[0].url
                        isResponseValid = !url.isNullOrBlank()
                    }

                    // 5. 每日推荐备用
                    if (!isResponseValid) {
                        val dailySongsResponse = NetWorkClient.apiService2.getListData()
                        if (dailySongsResponse.code != 200 || dailySongsResponse.data?.dailySongs.isNullOrEmpty()) {
                            throw Exception("每日推荐歌曲获取失败")
                        }
                        val backupSong = dailySongsResponse.data?.dailySongs?.get(0)
                        if (backupSong != null) {
                            musicUrlResponse = NetWorkClient.apiService3.getMusicUrl(backupSong.id.toString())

                            // 最终验证
                            if (musicUrlResponse.code == 200 && !musicUrlResponse.data.isNullOrEmpty()) {
                                val url = musicUrlResponse.data[0].url
                                isResponseValid = !url.isNullOrBlank()
                            }
                        }
                    }

                    if (!isResponseValid) {
                        throw Exception("所有渠道均未获取到有效播放地址")
                    }
                }

                // 6. 处理播放逻辑 - 修复回调逻辑
                withContext(Dispatchers.Main) {
                    val playUrl = musicUrlResponse.data?.get(0)?.url
                    if (playUrl.isNullOrBlank()) {
                        throw Exception("播放地址为空")
                    }
                    currentMusic = playUrl
                    // 修复：播放完成后不需要重新调用fetchMusic()，避免无限循环
                    playAudio(playUrl) {
                        // 播放完成后的回调逻辑（如需要自动播放下一首，可在这里实现）
                        // 暂时留空，或添加"播放完成"的提示
                        Log.d("MusicPlayer", "播放完成")
                    }
                    Log.d("MusicPlayer", "准备播放，URL: $playUrl")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MusicPlayerActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MusicPlayer", "获取音乐失败", e)
                }
            }
        }
    }

    private fun updateMusicInfo(songName: String, coverUrl: String) {
        binding.mpName.text = songListName
        binding.mpSinger.text=athour
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
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
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
        binding.mpStart.setImageResource(R.drawable.mp_stop)
        binding.mpStart.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
                binding.mpStart.setImageResource(R.drawable.mp_start)
                rotateRecord(-90f)
                stopCenterImg()
            } else {
                // 防止重复点击
                binding.mpStart.isEnabled = false
                binding.mpStart.setImageResource(R.drawable.mp_stop)

                if (mediaPlayer == null) {
                    currentMusic.takeIf { !it.isNullOrBlank() }?.let { url ->
                        playAudio(url) {
                            binding.mpStart.isEnabled = true
                        }
                    } ?: run {
                        Toast.makeText(this, "音乐加载中，请稍候", Toast.LENGTH_SHORT).show()
                        fetchMusic()
                        binding.mpStart.isEnabled = true
                    }
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    handler.post(updateSeekBarRunnable)
                    binding.mpStart.isEnabled = true
                    rotateRecord(90f)
                    startCenterImg()
                }
            }
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

    private fun rotateRecord(degree: Float) {
        // 确保控件测量完成后再设置旋转中心
        binding.mpRecord.post {
            binding.mpRecord.pivotX = binding.mpRecord.width / 2f
            binding.mpRecord.pivotY = binding.mpRecord.height / 2f
            binding.mpRecord.animate().cancel()
            binding.mpRecord.animate()
                .rotationBy(degree)
                .setDuration(1000)
                .withEndAction {
                    currentRotation += degree
                }
                .start()
        }
    }

    // 增加回调参数，处理异步状态
    private fun playAudio(url: String, onComplete: () -> Unit) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    binding.mpSeekBar.max = mp.duration
                    binding.mpTimend.text = formatTime(mp.duration)
                    binding.mpTimestart.text = formatTime(0)
                    mp.start()
                    this@MusicPlayerActivity.isPlaying = true
                    handler.post(updateSeekBarRunnable)
                    Log.d("MusicPlayer", "开始播放: $url")
                    rotateRecord(90f)
                    startCenterImg()
                    onComplete()
                }
                setOnCompletionListener {
                    Log.d("MusicPlayer", "播放完成")
                    resetPlayerState()
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicPlayer", "播放错误: what=$what, extra=$extra")
                    resetPlayerState()
                    onComplete()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MusicPlayer", "播放异常: ${e.message}")
            resetPlayerState()
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun resetPlayerState() {
        isPlaying = false
        binding.mpStart.setImageResource(R.drawable.mp_start)
        binding.mpStart.isEnabled = true // 恢复按钮可用
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        binding.mpSeekBar.progress = 0
        binding.mpTimestart.text = formatTime(0)
        currentRotation = 0f
        binding.mpRecord.rotation = currentRotation
        binding.mpRecord.animate().cancel()
        stopCenterImg()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
        stopCenterImg()
    }
}
