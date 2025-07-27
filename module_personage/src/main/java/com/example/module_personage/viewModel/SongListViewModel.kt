package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.bean.ListResult
import com.example.module_personage.bean.ListsResultData
import com.example.module_personage.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongListViewModel : ViewModel() {

    private val _listsResult = MutableStateFlow(ListsResultData(0, ListResult(0, emptyList()) ))
    val listsResult: StateFlow<ListsResultData> = _listsResult
        .filterNotNull() // 过滤掉null值
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ListsResultData(0,ListResult(0,emptyList()))
        )
    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getListsData() {
        if (_loadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getSongLists()
                Log.d("ListViewModel", "获取成功 ${response.body()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _listsResult.value = data
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
                    Log.e("ListViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("ListViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}