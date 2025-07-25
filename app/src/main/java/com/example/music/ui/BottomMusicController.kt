package com.example.music.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.viewmodel.BottomViewModel
import data.ListMusicData
import com.example.module_recommened.R as RecommendedR

class BottomMusicController(
    private val context: Context,
    private val view: View
) {
    private val viewModel: BottomViewModel
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // 可以在这里更新播放进度等UI
            handler.postDelayed(this, 1000)
        }
    }

    // 动画相关变量
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 360f // 完整旋转一周
    private var isAnimationRunning = false
    private var currentRotation = 0f
    private val animationDuration = 15000L

    init {
        // 初始化ViewModel
        viewModel = ViewModelProvider(context as AppCompatActivity)[BottomViewModel::class.java]

        // 初始化视图和事件
        initView()
        initClickEvents()
        initObservers()
        initAnimation()

        // 初始化服务连接
        viewModel.initService(context)
    }

    private fun initView() {
        // 初始化视图状态
        val ivCover = view.findViewById<ImageView>(R.id.music)
        val playButton = view.findViewById<ImageView>(R.id.stop)
        ivCover.rotation = ROTATION_PAUSED
        playButton.setImageResource(RecommendedR.drawable.list_start)
    }

    private fun initAnimation() {
        // 初始状态设置为暂停角度
        val ivCover = view.findViewById<ImageView>(R.id.music)
        ivCover.rotation = ROTATION_PAUSED
        currentRotation = ROTATION_PAUSED
    }

    private fun initClickEvents() {
        // 播放/暂停按钮点击事件
        val playButton = view.findViewById<ImageView>(R.id.stop)
        playButton.setOnClickListener {
            val success = viewModel.togglePlayPause()
            if (!success) {
                Toast.makeText(context, "还没有歌曲哟", Toast.LENGTH_SHORT).show()
            }
        }

        // 底部栏封面点击事件
        view.findViewById<ImageView>(R.id.music).setOnClickListener {
            val success = viewModel.handleCoverClick(context)
            if (!success) {
                Toast.makeText(context, "还没有歌曲哟!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initObservers() {
        // 观察歌曲变化
        viewModel.currentSong.observe(context as AppCompatActivity) { song ->
            updateSongInfo(song)
        }

        // 观察播放状态变化
        viewModel.isPlaying.observe(context as AppCompatActivity) { isPlaying ->
            updatePlayState(isPlaying)
        }
    }

    private fun updateSongInfo(song: ListMusicData.Song?) {
        val tvSong = view.findViewById<TextView>(R.id.tv_song)
        val tvArtist = view.findViewById<TextView>(R.id.tv_artist)
        val ivCover = view.findViewById<ImageView>(R.id.music)

        if (song != null) {
            tvSong.text = song.name
            tvArtist.text = song.ar.joinToString { it.name }
            Glide.with(context)
                .load(song.al.picUrl)
                .error(R.drawable.drawerimg)
                .into(ivCover)

            // 切换歌曲时重置动画状态
            resetAnimationOnSongChange()
        }
    }

    private fun updatePlayState(isPlaying: Boolean) {
        val playButton = view.findViewById<ImageView>(R.id.stop)
        val ivCover = view.findViewById<ImageView>(R.id.music)

        // 更新按钮图标
        playButton.setImageResource(
            if (isPlaying) R.drawable.now_play
            else RecommendedR.drawable.list_start
        )

        // 控制封面旋转动画
        if (isPlaying) {
            startCoverRotation(ivCover)
        } else {
            pauseCoverRotation(ivCover)
        }
    }

    private fun startCoverRotation(ivCover: ImageView) {
        if (isAnimationRunning) return

        isAnimationRunning = true
        // 取消之前的动画
        ivCover.animate().cancel()

        // 计算剩余旋转角度
        val remainingRotation = ROTATION_PLAYING - currentRotation % ROTATION_PLAYING

        // 开始旋转动画
        ivCover.animate()
            .rotation(currentRotation + remainingRotation)
            .setDuration((remainingRotation / ROTATION_PLAYING * animationDuration).toLong())
            .withEndAction {
                // 完成一次旋转后重新开始，形成无限循环
                currentRotation = 0f
                startCoverRotation(ivCover)
            }
            .start()
    }

    private fun pauseCoverRotation(ivCover: ImageView) {
        if (!isAnimationRunning) return

        // 记录当前旋转角度
        currentRotation = ivCover.rotation
        // 停止动画
        ivCover.animate().cancel()
        isAnimationRunning = false
    }

    private fun resetAnimationOnSongChange() {
        val ivCover = view.findViewById<ImageView>(R.id.music)
        // 重置旋转状态
        currentRotation = 0f
        ivCover.rotation = currentRotation

        // 如果正在播放，重新开始动画
        if (isAnimationRunning) {
            startCoverRotation(ivCover)
        }
    }

    fun onDestroy() {
        // 停止 Handler 任务
        handler.removeCallbacksAndMessages(null)

        // 停止动画
        val ivCover = view.findViewById<ImageView>(R.id.music)
        ivCover.animate().cancel()
        isAnimationRunning = false
    }
}
