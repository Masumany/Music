package com.example.module_mvplayer.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.BuildConfig
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.module_mvplayer.R
import com.example.module_mvplayer.viewModel.player.PlayBackState
import com.example.module_mvplayer.viewModel.player.PlayerState
import com.example.module_mvplayer.databinding.ActivityMvPlayerBinding
import com.example.module_mvplayer.ui.fragment.CommentBottomSheetDialogFragment
import com.example.module_mvplayer.viewModel.LoadState
import com.example.module_mvplayer.viewModel.MvPlayerViewModel
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import kotlinx.coroutines.flow.collectLatest

//@JvmField
//@Autowired
//var mvId: String = ""


@Route(path = "/module_mvplayer/mvplayer")
class MvPlayerActivity : AppCompatActivity() {
    private   var mvId: String=""
    private lateinit var binding: ActivityMvPlayerBinding
    private val viewModel: MvPlayerViewModel by viewModels()
    private var player: ExoPlayer? = null
    private var mvPlayUrl: String? = null
    private var currentPlaybackState = PlayBackState.IDLE
    private var isFullscreen = false //全屏状态标记



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMvPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TheRouter.inject( this)

        Log.d("MvPlayerActivity", "mvId: $mvId")

        mvId = intent.getStringExtra("mvId") ?: ""
        Log.d("MvPlayerActivity", "从Intent获取mvId: $mvId")


        // 初始化播放器
        initPlayer()

        collectPlayUrl()
        collectMvInfo()
        collectMvData()

        initClick()
    }

    // dp转px工具方法
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun initPlayer() {
        // 创建ExoPlayer，配置到PlayerView中
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

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
        // 设置控制器可见性监听器
        binding.playerView.setControllerVisibilityListener (PlayerView.ControllerVisibilityListener { visibility ->
            // 同步全屏按钮的显示/隐藏状态
            binding.fullscreenButton.visibility = visibility
        })
    }

    private fun collectPlayUrl() {
        // 加载播放地址
        Log.d("MvPlayerActivity", "准备调用 viewModel.loadMvPlayUrl")
        viewModel.loadMvPlayUrl(mvId)
        lifecycleScope.launchWhenStarted {
            viewModel.mvPlayUrl.collectLatest { result ->
                Log.d("MvPlayerActivity", "mvPlayUrl collected: $result")
                val url = result?.data?.url ?: return@collectLatest
                mvPlayUrl = url
                val mediaItem = MediaItem.fromUri(url)
                player?.setMediaItem(mediaItem)
                player?.prepare()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbMv.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbMv.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbMv.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbMv.visibility = android.view.View.GONE
                        Toast.makeText(this@MvPlayerActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbMv.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }


    private fun collectMvInfo() {
        viewModel.loadMvInfo(mvId)
        lifecycleScope.launchWhenStarted {
            viewModel.mvInfo.collectLatest {
                it?.let {
                    binding.mvLikeTv.text = it.likedCount.toString()
                    binding.mvShareTv.text = it.shareCount.toString()
                    binding.mvCommentTv.text = it.commentCount.toString()
                }
            }
        }
    }

    private fun collectMvData() {
        viewModel.loadMvData(mvId)
        lifecycleScope.launchWhenStarted {
            viewModel.mvDetail.collectLatest {
                it?.let {
                    val mvItem = it.data
                    binding.mvAuthorTv.text = mvItem.artistName?: "未知作者"

                    if (!mvItem.cover.isNullOrEmpty()) {
                        Glide.with(this@MvPlayerActivity)
                            .load(mvItem.cover)
                            .circleCrop()
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.error)
                            .into(binding.mvAuthorImg)
                    } else {
                        binding.mvAuthorImg.setImageResource(R.drawable.error)
                    }
                    binding.mvDescTv.text = mvItem.mv?.desc ?: "暂无描述"
                }
            }
        }
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
            mvPlayUrl?.let {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, "分享 MV")
                    putExtra(Intent.EXTRA_TEXT, "快来看看这个 MV：$it")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "分享到..."))
            }
            }
    }

    private fun updatePlaybackState(state: PlayerState) {
        // 避免重复显示相同状态的提示
        if (state.playBackState == currentPlaybackState) return
        currentPlaybackState = state.playBackState
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun fullScreen() {
        isFullscreen = !isFullscreen
        // 使用 ConstraintSet 来管理布局约束
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.main)
        if (isFullscreen) {
//            //强制横屏
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI()
            // 设置 PlayerView 全屏约束
            constraintSet.constrainHeight(binding.playerView.id, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.connect(binding.playerView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(binding.playerView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            // 清除所有约束并隐藏
            constraintSet.clear(binding.mvInfo.id)
            constraintSet.clear(binding.mvBackLayout.id)
            binding.mvInfo.visibility = View.GONE
            binding.mvBackLayout.visibility = View.GONE
            binding.root.requestLayout()
            binding.fullscreenButton.setBackgroundResource(R.drawable.exit_full)
        } else {
            //恢复竖屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showSystemUI()
            // 设置 PlayerView 默认高度约束
            constraintSet.constrainHeight(binding.playerView.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.clear(binding.playerView.id, ConstraintSet.BOTTOM) // 清除底部约束
            constraintSet.connect(binding.playerView.id, ConstraintSet.TOP, binding.mvBack.id, ConstraintSet.BOTTOM, dp2px(this, 5f))
            binding.mvInfo.visibility = View.VISIBLE
            binding.mvBackLayout.visibility = View.VISIBLE
            binding.fullscreenButton.setBackgroundResource(R.drawable.full)
        }
        //应用约束
        constraintSet.applyTo(binding.main)
    }

    // 生命周期
    override fun onStart() {
        super.onStart()
        if (player == null) {
            initPlayer()
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