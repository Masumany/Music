package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.bean.history.HistoryData
import com.example.module_personage.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _historyData = MutableStateFlow<HistoryData>(HistoryData(0, emptyList()))
    val historyData: StateFlow<HistoryData> = _historyData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun loadHistory(uid: String, type: Int = 1) {
        if (_loadState.value is LoadState.Loading) {
            return
        }

        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading

                val response = NetRepository.apiService.getHistory()
                Log.d("HistoryViewModel", "接口请求结果：${response.message()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _historyData.value = data
                        _loadState.value = LoadState.Success
                    } else {
                        _loadState.value = LoadState.Error("获取失败：数据为空")
                    }
                } else {
                    _loadState.value = LoadState.Error("请求失败：${response.code()}")
                    Log.e("HistoryViewModel", "获取失败：${response.code()}")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                Log.e("HistoryViewModel", "获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}