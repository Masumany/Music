package com.example.module_mvplayer.ui.activity

import android.content.Intent
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.module_mvplayer.R
import com.example.module_mvplayer.viewModel.player.PlayBackState
import com.example.module_mvplayer.viewModel.player.PlayerState
import com.example.module_mvplayer.databinding.ActivityMvPlayerBinding
import com.example.module_mvplayer.ui.fragment.CommentBottomSheetDialogFragment
import com.example.module_mvplayer.viewModel.MvPlayerViewModel
import kotlinx.coroutines.flow.collectLatest

class MvPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMvPlayerBinding
    private val viewModel: MvPlayerViewModel by viewModels()
    private var player: ExoPlayer? = null
    private lateinit var mvId: String
    private var mvPlayUrl: String? = null
    private var currentPlaybackState = PlayBackState.IDLE
    private var isFullscreen = false //全屏状态标记

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMvPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 创建ExoPlayer，配置到PlayerView中
        binding.playerView.player = ExoPlayer.Builder(this)
            .build()

        binding.playerView.player?.run {
            // 设置播放监听
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    // 播放状态变化回调
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    // 播放状态变化回调
                    when (playbackState) {
                        Player.STATE_READY -> {

                        }

                        Player.STATE_BUFFERING -> {

                        }

                        Player.STATE_ENDED -> {
                            Toast.makeText(this@MvPlayerActivity, "播放结束", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    // 播放错误回调
                }
            })
            //无限重复
            repeatMode = Player.REPEAT_MODE_ALL
            // 设置当缓冲完毕后直接播放视频
            playWhenReady = true
        }
    }

//    private fun collectMvInfo() {
//        viewModel.loadMvInfo(mvId)
//        lifecycleScope.launchWhenStarted {
//            viewModel.mvInfo.collectLatest {
//                it?.let {
//                    binding.mvLikeTv.text = it.likedCount.toString()
//                    binding.mvShareTv.text = it.shareCount.toString()
//                    binding.mvCommentTv.text = it.commentCount.toString()
//                }
//            }
//        }
//    }
//
//    private fun collectMvData() {
//        viewModel.loadMvData(mvId)
//        lifecycleScope.launchWhenStarted {
//            viewModel.mvDetail.collectLatest {
//                it?.let {
//                    val mvItem = it.data.firstOrNull() ?: return@let
//                    binding.mvAuthorTv.text = mvItem.artistName
//
//                    Glide.with(this@MvPlayerActivity)
//                        .load(mvItem.cover)
//                        .circleCrop()
//                        .placeholder(R.drawable.loading)
//                        .error(R.drawable.error)
//                        .into(binding.mvAuthorImg)
//
//                    binding.mvDescTv.text = mvItem.mv.desc
//                }
//            }
//        }
//    }
//
//    private fun collectMvPlayUrl() {
//        viewModel.loadMvPlayUrl(mvId)
//        lifecycleScope.launchWhenStarted {
//            viewModel.mvPlayUrl.collect {
//                mvPlayUrl = it?.data?.url
//            }
//        }
//    }
//
//    private fun initClick() {
//        binding.mvBack.setOnClickListener {
//            finish()
//        }
////        binding.fullscreenButton.setOnClickListener {
////            fullScreen()
////        }
//        binding.mvCommentButton.setOnClickListener {
//            // 跳转到评论页面
//            val dialog = CommentBottomSheetDialogFragment(mvId)
//            dialog.show(supportFragmentManager, "CommentBottomSheet")
//        }
//        binding.mvLikeButton.setOnClickListener {
//            // 跳转到收藏页面
//        }
//        binding.mvShareButton.setOnClickListener {
//            // 跳转到分享页面
//            mvPlayUrl?.let {
//                val shareIntent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_SUBJECT, "分享 MV")
//                    putExtra(Intent.EXTRA_TEXT, "快来看看这个 MV：$it")
//                    type = "text/plain"
//                }
//                startActivity(Intent.createChooser(shareIntent, "分享到..."))
//            }
//            }
//    }
//
//    private fun updatePlaybackState(state: PlayerState) {
//        // 避免重复显示相同状态的提示
//        if (state.playBackState == currentPlaybackState) return
//        currentPlaybackState = state.playBackState
//    }

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

//    private fun fullScreen() {
//        isFullscreen = !isFullscreen
//        if (isFullscreen) {
//            hideSystemUi()
//            binding.playerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//            binding.mvAuthorTv.visibility = View.GONE
//            binding.mvAuthorImg.visibility = View.GONE
//            binding.mvLikeButton.visibility = View.GONE
//            binding.mvCommentButton.visibility = View.GONE
//            binding.mvShareButton.visibility = View.GONE
//            binding.mvLikeTv.visibility = View.GONE
//            binding.mvShareTv.visibility = View.GONE
//            binding.mvCommentTv.visibility = View.GONE
////            binding.fullscreenButton.setBackgroundResource(R.drawable.exit_full)
//        } else {
//            showSystemUi()
//            binding.playerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            binding.mvAuthorTv.visibility = View.VISIBLE
//            binding.mvAuthorImg.visibility = View.VISIBLE
//            binding.mvLikeButton.visibility = View.VISIBLE
//            binding.mvCommentButton.visibility = View.VISIBLE
//            binding.mvShareButton.visibility = View.VISIBLE
//            binding.mvLikeTv.visibility = View.VISIBLE
//            binding.mvShareTv.visibility = View.VISIBLE
//            binding.mvCommentTv.visibility = View.VISIBLE
////            binding.fullscreenButton.setBackgroundResource(R.drawable.full)
//        }
//        //刷新
//        binding.playerView.requestLayout()
//    }
//    private fun Int.dpToPx(): Int {
//        return (this * resources.displayMetrics.density + 0.5f).toInt()
//    }
    // 生命周期
    override fun onStart() {
        super.onStart()
        if (player == null) {
//            initializePlayer()
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