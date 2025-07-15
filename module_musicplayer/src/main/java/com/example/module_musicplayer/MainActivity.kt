package com.example.module_musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var startImg: ImageView
    private var isPlaying = false
    private lateinit var timeStart:TextView
    private lateinit var timeEnd:TextView
    private lateinit var seekBar: SeekBar
    private lateinit var likeImg: ImageView
    private lateinit var recordImg: ImageView
    private var isLiked = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    val currentPosition = it.currentPosition
                    seekBar.progress = currentPosition
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seekBar)
        startImg = findViewById(R.id.start)
        timeStart = findViewById(R.id.timestart)
        timeEnd = findViewById(R.id.timend)
        likeImg = findViewById(R.id.like)
        recordImg= findViewById(R.id.record)

        likeImg.setOnClickListener{
            isLiked = !isLiked
            if (isLiked) {
                likeImg.setImageResource(R.drawable.like_last)
                Toast.makeText(this, "已收藏", Toast.LENGTH_LONG).show()
            }else{
                likeImg.setImageResource(R.drawable.like)
                Toast.makeText(this, "已取消", Toast.LENGTH_LONG).show()
            }
        }

        initSeekBarListener()
        initPlayButton()
    }
    fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun initSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 0
                timeStart.text = formatTime(currentPosition)
                timeEnd.text = formatTime(duration)
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
        startImg.setImageResource(R.drawable.start)
        startImg.setOnClickListener {
            if (isPlaying) {
                recordImg.pivotX= (recordImg.width / 2).toFloat()
                recordImg.pivotY= (recordImg.height / 2).toFloat()
                recordImg.animate().rotationBy(-90f).setDuration(1000).start()
                pauseAudio()
                startImg.setImageResource(R.drawable.start)
            } else {
                startImg.setImageResource(R.drawable.stop)
                if (mediaPlayer==null) {
                    playAudio("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    handler.post(updateSeekBarRunnable)
                }
            }
        }
    }

    private fun playAudio(url: String) {
        try {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    seekBar.max = mp.duration
                    timeEnd.text = formatTime(mp.duration)
                    mp.start()
                    this@MainActivity.isPlaying = true
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
        // 暂停时停止更新进度条
        handler.removeCallbacks(updateSeekBarRunnable)
        Log.d("MainActivity", "暂停播放")
    }

    private fun resetPlayerState() {
        isPlaying = false
        startImg.setImageResource(R.drawable.start)
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        seekBar.progress = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
