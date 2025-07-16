package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.bean.liked.LikedData
import com.example.module_personage.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LikedViewModel : ViewModel(){
    private val _likedData = MutableStateFlow(LikedData(0, emptyList(), false, 0))
    val likedData: StateFlow<LikedData> = _likedData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getLikedData(id: Int) {
        if (_loadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getFolloweds(id.toString())
                Log.d("LikedViewModel", "获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _likedData.value = data
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
                    Log.e("LikedViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("LikedViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}