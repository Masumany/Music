package com.example.module_mvplayer.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.module_mvplayer.repositorty.NetRepository
import com.example.module_mvplayer.bean.mvData.MvData
import com.example.module_mvplayer.bean.mvInfo.MvInfoData
import com.example.module_mvplayer.bean.mvPlayUrl.MvPlayUrl
import com.example.module_mvplayer.viewModel.player.PlayBackState
import com.example.module_mvplayer.viewModel.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    fun loadMvData(mvId: String) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val mvDetail = NetRepository.apiService.getMvDetail(mvId!!)
                Log.d("MvData", "code=${mvDetail.code()}, message=${mvDetail.message()}")
                if (mvDetail.isSuccessful) {
                    _mvDetail.value = mvDetail.body()
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("获取MV详情失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MvData", "loadMvData: ${e.localizedMessage}")
                _loadState.value = LoadState.Error("网络错误: ${e.localizedMessage}")
            }
        }
    }
    fun loadMvInfo(mvId: String) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val response = NetRepository.apiService.getMvInfo(mvId)
                Log.d("MvInfo", "code=${response.code()}, message=${response.message()}")
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
        Log.d("MvPlayerViewModel", "loadMvPlayUrl called with mvId = $mvId")
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val response =NetRepository.apiService.getMvPlayUrl(mvId)
                Log.d("MvPlayerViewModel", "code=${response.code()}, message=${response.message()}")
                Log.d("MvPlayerViewModel", "loadMvPlayUrl: ${response.body()}")
                if (response.isSuccessful) {
                    _mvPlayUrl.value = response.body()
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("获取播放地址失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MvPlayerViewModel", "loadMvPlayUrl: ${e.localizedMessage}")
                _loadState.value = LoadState.Error("网络错误: ${e.localizedMessage}")
            }
        }
    }

    //清理资源
    override fun onCleared() {
        super.onCleared()
        _playState.value = PlayerState(PlayBackState.IDLE)
        clearState()
    }

    fun clearState() {
        _playState.value = PlayerState(PlayBackState.IDLE)
    }
}