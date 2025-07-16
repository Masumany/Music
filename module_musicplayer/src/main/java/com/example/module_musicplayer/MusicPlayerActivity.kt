package com.example.module_musicplayer

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
import com.example.module_musicplayer.databinding.MusicPlayerBinding


class MusicPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var binding: MusicPlayerBinding
    private var fragment: Fragment? = null
    private var currentRotation = 0f
    private var animation: Animation? = null
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())
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

        // 初始化ViewBinding，与ConstraintLayout根布局匹配
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化Fragment
        fragment = supportFragmentManager.findFragmentById(binding.mpFragment.id)
        fragment?.let {
            supportFragmentManager.beginTransaction().hide(it).commit()
        }

        // 返回按钮点击事件
        binding.mpMback.setOnClickListener {
            finish()
        }

        // 评论按钮点击事件
        binding.mpCommon.setOnClickListener {
            changeFragment()
        }

        // 收藏按钮点击事件
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

        // 初始化中心图片动画
        initCenterImg()
        // 初始化进度条监听
        initSeekBarListener()
        // 初始化播放按钮
        initPlayButton()
    }

    private fun changeFragment() {
        fragment?.let { frag ->
            val transaction = supportFragmentManager.beginTransaction()
            if (frag.isHidden) {
                transaction.show(frag)
                Log.d("MainActivity", "显示评论碎片")
            } else {
                transaction.hide(frag)
                Log.d("MainActivity", "隐藏评论碎片")
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
        animation = AnimationUtils.loadAnimation(this, R.anim.img_animation)
        val linearInterpolator = LinearInterpolator()
        animation?.interpolator = linearInterpolator
    }

    // 格式化时间（毫秒转分:秒）
    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun initSeekBarListener() {
        // 使用正确的SeekBar.OnSeekBarChangeListener
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
        binding.mpStart.setImageResource(R.drawable.mp_start)
        binding.mpStart.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
                binding.mpStart.setImageResource(R.drawable.mp_start)
                rotateRecord(-90f)
                stopCenterImg()
            } else {
                binding.mpStart.setImageResource(R.drawable.mp_stop)
                if (mediaPlayer == null) {
                    playAudio("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    handler.post(updateSeekBarRunnable)
                }
                rotateRecord(90f)
                startCenterImg()
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
        binding.mpRecord.pivotX = (binding.mpRecord.width / 2).toFloat()
        binding.mpRecord.pivotY = (binding.mpRecord.height / 2).toFloat()
        binding.mpRecord.animate().cancel()
        binding.mpRecord.animate()
            .rotationBy(degree)
            .setDuration(1000)
            .withEndAction {
                currentRotation += degree
            }
            .start()
    }

    private fun playAudio(url: String) {
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
                    Log.d("MainActivity", "开始播放")
                }
                setOnCompletionListener {
                    Log.d("MainActivity", "播放完成")
                    resetPlayerState()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MainActivity", "播放错误: what=$what, extra=$extra")
                    resetPlayerState()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "播放异常: ${e.message}")
            resetPlayerState()
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        handler.removeCallbacks(updateSeekBarRunnable)
        Log.d("MainActivity", "暂停播放")
    }

    private fun resetPlayerState() {
        isPlaying = false
        binding.mpStart.setImageResource(R.drawable.mp_start)
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
