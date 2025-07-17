package com.example.module_mvplayer.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.module_mvplayer.bean.player.PlayBackState
import com.example.module_mvplayer.bean.player.PlayerState
import com.example.module_mvplayer.databinding.ActivityMvPlayerBinding
import com.example.module_mvplayer.viewModel.MvPlayerViewModel

class MvPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMvPlayerBinding
    private val viewModel: MvPlayerViewModel by viewModels()
    private var player: ExoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMvPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mvId = intent.getStringExtra("mvId")
        if (mvId != null) {
            viewModel.setMvId(mvId)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.playState.collect {
                updateUi(it)
            }
        }

        initializePlayer()
    }
    private fun initializePlayer() {
        player = viewModel.createPlayer(this)
        binding.playerView.player = player

        intent.getStringExtra("mvId")?.let {
            viewModel.preparePlayer(player!!, it)
        }

        player?.addListener(viewModel.playerListeners(player !!))
    }
    private fun updateUi(state: PlayerState) {
        when(state.playBackState){
            PlayBackState.BUFFERING -> {
                Toast.makeText(this, "正在缓冲", Toast.LENGTH_SHORT).show()
            }
            PlayBackState.ERROR -> {
                Toast.makeText(this, "播放错误", Toast.LENGTH_SHORT).show()
            }
            PlayBackState.ENDED -> {
                Toast.makeText(this, "播放结束", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}