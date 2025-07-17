package com.example.module_mvplayer.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.module_mvplayer.bean.player.PlayBackState
import com.example.module_mvplayer.bean.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MvPlayerViewModel : ViewModel() {
    private val _playState = MutableStateFlow(PlayerState())
    val playState: StateFlow<PlayerState> = _playState.asStateFlow()

    //快退增量
    var seekBackIncrementMs: Long = 5000
    //快进增量
    var seekForwardIncrementMs: Long = 5000

    private var mvId : String? = null

    fun setMvId(id: String?) {
        mvId = id
    }

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(seekBackIncrementMs)
            .setSeekForwardIncrementMs(seekForwardIncrementMs)
            .build()
    }
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun preparePlayer(player: ExoPlayer, mvId: String) {
        val mediaItem = MediaItem.fromUri(mvId)
        player.setMediaItem(mediaItem)
        player.prepare()
    }
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun playerListeners(player: ExoPlayer): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val state = when (playbackState) {
                    Player.STATE_IDLE -> PlayBackState.IDLE
                    Player.STATE_BUFFERING -> PlayBackState.BUFFERING
                    Player.STATE_READY -> PlayBackState.READY
                    Player.STATE_ENDED -> PlayBackState.ENDED
                    else -> PlayBackState.ERROR
                }
                _playState.update {
                    it.copy(
                        playBackState = state,
                        isPlaying = playbackState == Player.STATE_READY && player.playWhenReady
                    )
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                _playState.update { it.copy(
                    playBackState = PlayBackState.ERROR,
                    errorMessage = error.message
                ) }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playState.update { it.copy(isPlaying = isPlaying)}
            }

            @OptIn(androidx.media3.common.util.UnstableApi::class)
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playState.update { it.copy(currentPosition = newPosition.positionMs) }
            }
        }
     }

    fun positionUpdates(player: ExoPlayer){
        player.playWhenReady = !player.playWhenReady
    }

    fun seekForward(player: ExoPlayer){
        val newPosition = player.currentPosition + seekForwardIncrementMs
        player.seekTo(newPosition)
    }

    fun seekBack(player: ExoPlayer){
        val newPosition = player.currentPosition - seekBackIncrementMs
        player.seekTo(newPosition)
    }

    //跳转到指定位置
    fun seekTo(player: ExoPlayer, position: Long){
        player.seekTo(position)
    }

    override fun onCleared() {
        super.onCleared()
        _playState.value = PlayerState(PlayBackState.IDLE)
    }
}