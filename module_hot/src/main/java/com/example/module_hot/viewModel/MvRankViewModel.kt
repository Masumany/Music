package com.example.module_hot.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_hot.bean.mv.MvRankData
import com.example.module_hot.bean.singer.SingerData
import com.example.module_hot.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MvRankViewModel : ViewModel() {
    private val _mvData = MutableStateFlow(MvRankData(0, emptyList(),false,0))
    val mvData: StateFlow<MvRankData> = _mvData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getMvData() {
        if (_loadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getMvRank()
                Log.d("MvRankViewModel", "获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _mvData.value = data
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
                    Log.e("MvRankViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("MvRankViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}