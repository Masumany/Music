package com.example.module_mvplayer.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_mvplayer.Repositorty.NetRepository
import com.example.module_mvplayer.bean.mvData.MvData
import com.example.module_mvplayer.bean.mvInfo.MvInfoData
import com.example.module_mvplayer.bean.mvPlayUrl.MvPlayUrl
import com.example.module_mvplayer.bean.player.PlayBackState
import com.example.module_mvplayer.bean.player.PlayerState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MvPlayerViewModel : ViewModel() {
    private val _playState = MutableStateFlow(PlayerState())
    val playState: StateFlow<PlayerState> = _playState.asStateFlow()

    private val _mvDetail = MutableStateFlow<MvData?>(null)
    val mvDetail: StateFlow<MvData?> = _mvDetail.asStateFlow()

    private val _mvInfo = MutableStateFlow<MvInfoData?>(null)
    val mvInfo: StateFlow<MvInfoData?> = _mvInfo.asStateFlow()

    private val _mvPlayUrl = MutableStateFlow<MvPlayUrl?>(null)
    val mvPlayUrl: StateFlow<MvPlayUrl?> = _mvPlayUrl.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    var seekBackIncrementMs: Long = 5000
    var seekForwardIncrementMs: Long = 5000

    private var mvId: String? = null

    fun setMvId(id: String?) {
        mvId = id
    }

    fun getMvId(): String? {
        return mvId
    }

    fun loadMvData(mvId: String) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val mvDetail = NetRepository.apiService.getMvDetail(mvId!!)
                if (mvDetail.isSuccessful) {
                    _mvDetail.value = mvDetail.body()
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("获取MV详情失败")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("网络错误: ${e.localizedMessage}")
            }
        }
    }
    fun loadMvInfo(mvId: String) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val response = NetRepository.apiService.getMvInfo(mvId)
                if (response.isSuccessful) {
                    _mvInfo.value = response.body()
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("获取信息失败")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("网络错误: ${e.localizedMessage}")
            }
        }
    }
    fun loadMvPlayUrl(mvId: String) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val response =NetRepository.apiService.getMvPlayUrl(mvId)
                if (response.isSuccessful) {
                    _mvPlayUrl.value = response.body()
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("获取播放地址失败")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("网络错误: ${e.localizedMessage}")
            }
        }
    }

    // 创建ExoPlayer实例
    fun createPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(seekBackIncrementMs)
            .setSeekForwardIncrementMs(seekForwardIncrementMs)
            .build()
    }

    fun preparePlayer(player: ExoPlayer, mvId: String) {
        val mediaItem = MediaItem.fromUri(mvId)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    // 绑定播放器监听器
    fun attachPlayerListeners(player: ExoPlayer) {
        player.addListener(object : Player.Listener {
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
                _playState.update {
                    it.copy(
                        playBackState = PlayBackState.ERROR,
                        errorMessage = error.message
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playState.update { it.copy(currentPosition = newPosition.positionMs) }
            }
        })
    }

    // 切换播放状态
    fun togglePlayback(player: ExoPlayer) {
        player.playWhenReady = !player.playWhenReady
    }

    fun seekForward(player: ExoPlayer) {
        val newPosition = player.currentPosition + seekForwardIncrementMs
        player.seekTo(newPosition)
    }

    fun seekBack(player: ExoPlayer) {
        val newPosition = player.currentPosition - seekBackIncrementMs
        player.seekTo(newPosition.coerceAtLeast(0))// 防止负数
    }

    // 跳转到指定位置
    fun seekTo(player: ExoPlayer, position: Long) {
        player.seekTo(position)
    }

    //清理资源
    override fun onCleared() {
        super.onCleared()
        _playState.value = PlayerState(PlayBackState.IDLE)
        clearState()
    }

    fun clearState() {
        mvId = null
        _playState.value = PlayerState(PlayBackState.IDLE)
    }
}