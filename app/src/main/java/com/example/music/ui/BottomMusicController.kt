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

    // 动画相关变量
    private val ROTATION_PAUSED = 0f
    private val ROTATION_PLAYING = 360f // 完整旋转一周
    private var isAnimationRunning = false
    private var currentRotation = 0f  //当前的旋转角度
    private val animationDuration = 15000L  //唱片转一圈儿的时间

    init {
        viewModel = ViewModelProvider(context as AppCompatActivity)[BottomViewModel::class.java]

        initView()
        initClickEvents()
        initObservers()
        initAnimation()

        // 初始化服务连接
        viewModel.initService(context)
    }

    private fun initView() {
        val ivCover = view.findViewById<ImageView>(R.id.music)
        val playButton = view.findViewById<ImageView>(R.id.stop)
        ivCover.rotation = ROTATION_PAUSED  //初始封面不转动
        playButton.setImageResource(RecommendedR.drawable.list_start)
    }

    private fun initAnimation() {
        val ivCover = view.findViewById<ImageView>(R.id.music)
        ivCover.rotation = ROTATION_PAUSED  //初始状态设为0度也就是暂停时的角度
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
            updateSongInfo(song)  //更新ui
        }

        // 观察播放状态变化
        viewModel.isPlaying.observe(context as AppCompatActivity) { isPlaying ->
            updatePlayState(isPlaying)  //播放状态发生变化时，更新播放状态
        }
    }

    // 更新歌曲信息
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

    // 播放状态变化
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
            startCoverRotation(ivCover)  //播放时
        } else {
            pauseCoverRotation(ivCover)  //暂停时
        }
    }

    private fun startCoverRotation(ivCover: ImageView) {
        if (isAnimationRunning) return  //如果已经有动画了，直接返回，避免重复启动同一个动画

        isAnimationRunning = true
        // 取消之前的动画
        ivCover.animate().cancel()

        // 计算剩余旋转角度
        val remainingRotation = ROTATION_PLAYING - currentRotation % ROTATION_PLAYING

        // 开始旋转动画
        ivCover.animate()
            .rotation(currentRotation + remainingRotation)  //旋转到目标角度
            .setDuration((remainingRotation / ROTATION_PLAYING * animationDuration).toLong())  //匀速转动
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

    // 当歌曲切换时，重置动画状态
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
