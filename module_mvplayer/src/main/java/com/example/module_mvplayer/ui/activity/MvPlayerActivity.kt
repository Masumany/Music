package com.example.module_mvplayer.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.module_mvplayer.R
import com.example.module_mvplayer.bean.player.PlayBackState
import com.example.module_mvplayer.bean.player.PlayerState
import com.example.module_mvplayer.databinding.ActivityMvPlayerBinding
import com.example.module_mvplayer.ui.fragment.CommentBottomSheetDialogFragment
import com.example.module_mvplayer.viewModel.MvPlayerViewModel
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.flow.collectLatest

class MvPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMvPlayerBinding
    private val viewModel: MvPlayerViewModel by viewModels()
    private var player: ExoPlayer? = null
    private lateinit var mvId: String
    private var currentPlaybackState = PlayBackState.IDLE
    private var isFullscreen = false //全屏状态标记

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMvPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mvId = intent.getStringExtra("mvId") ?: run {
            Toast.makeText(this,"缺少MV ID", Toast.LENGTH_SHORT)
            finish()
            return
        }

        viewModel.setMvId(mvId)

        // 监听播放状态变化
        lifecycleScope.launchWhenStarted {
            viewModel.playState.collectLatest {
                updatePlaybackState(it)
            }
        }
        initClick()
    }

    private fun initClick() {
        binding.mvBack.setOnClickListener {
            finish()
        }
        binding.fullscreenButton.setOnClickListener {
            fullScreen()
        }
        binding.mvCommentButton.setOnClickListener {
            // 跳转到评论页面
            val dialog = CommentBottomSheetDialogFragment(mvId)
            dialog.show(supportFragmentManager, "CommentBottomSheet")
        }
        binding.mvLikeButton.setOnClickListener {
            // 跳转到收藏页面
        }
        binding.mvShareButton.setOnClickListener {
            // 跳转到分享页面
        }
    }
    private fun initializePlayer() {
        if (player == null) {
            player = viewModel.createPlayer(this).apply {
                //绑定播放器到视图
                binding.playerView.player = this
                //添加监听器
                viewModel.attachPlayerListeners(this)
            }
        }

        viewModel.getMvId()?.let { mvId ->
            viewModel.preparePlayer(player!!, mvId)
        }
    }

    private fun updatePlaybackState(state: PlayerState) {
        // 避免重复显示相同状态的提示
        if (state.playBackState == currentPlaybackState) return
        currentPlaybackState = state.playBackState

        when (state.playBackState) {
            PlayBackState.BUFFERING -> {
                Toast.makeText(this, "正在缓冲...", Toast.LENGTH_SHORT).show()
                binding.pbMv.visibility = View.VISIBLE
            }
            PlayBackState.READY -> {
                binding.pbMv.visibility = View.GONE
            }
            PlayBackState.ERROR -> {
                binding.pbMv.visibility = View.GONE
                Toast.makeText(this, "播放错误: ${state.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            PlayBackState.ENDED -> {
                Toast.makeText(this, "播放结束", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun fullScreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            hideSystemUi()
            binding.playerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.mvAuthorTv.visibility = View.GONE
            binding.mvAuthorImg.visibility = View.GONE
            binding.mvLikeButton.visibility = View.GONE
            binding.mvCommentButton.visibility = View.GONE
            binding.mvShareButton.visibility = View.GONE
            binding.mvLikeTv.visibility = View.GONE
            binding.mvShareTv.visibility = View.GONE
            binding.mvCommentTv.visibility = View.GONE
            binding.fullscreenButton.setBackgroundResource(R.drawable.exit_full)
        } else {
            showSystemUi()
            binding.playerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.mvAuthorTv.visibility = View.VISIBLE
            binding.mvAuthorImg.visibility = View.VISIBLE
            binding.mvLikeButton.visibility = View.VISIBLE
            binding.mvCommentButton.visibility = View.VISIBLE
            binding.mvShareButton.visibility = View.VISIBLE
            binding.mvLikeTv.visibility = View.VISIBLE
            binding.mvShareTv.visibility = View.VISIBLE
            binding.mvCommentTv.visibility = View.VISIBLE
            binding.fullscreenButton.setBackgroundResource(R.drawable.full)
        }
        //刷新
        binding.playerView.requestLayout()
    }
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }
    // 生命周期
    override fun onStart() {
        super.onStart()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        viewModel.clearState()
    }
}