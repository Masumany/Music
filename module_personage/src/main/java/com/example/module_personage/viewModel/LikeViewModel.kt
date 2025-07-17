package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.bean.like.Follow
import com.example.module_personage.bean.like.LikeData
import com.example.module_personage.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LikeViewModel: ViewModel() {
    private val _likeData = MutableStateFlow(LikeData(0, emptyList(), false, 0))
    val likeData: StateFlow<LikeData> = _likeData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getLikeData(id: Int) {
        if (_loadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getFollows(id.toString())
                Log.d("LikeViewModel", "获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _likeData.value = data
                        _loadState.value = LoadState.Success
                    }else{
                        if (data == null){
                            _loadState.value = LoadState.Error("获取失败 ${response.message()}")
                        }else{
                            _loadState.value = LoadState.Error("未知错误 ${response.message()}")
                        }
                    }

                }else{
                    _loadState.value = LoadState.Error("获取失败 ${response.message()}")
                    Log.e("LikeViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("LikeViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}