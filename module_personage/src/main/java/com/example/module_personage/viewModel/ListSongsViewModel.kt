package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.repository.NetRepository
import com.example.moudle_search.bean.list_songs.ListSongsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ListSongsViewModel: ViewModel() {
    private val _listSongsData = MutableStateFlow(ListSongsData(0, emptyList(), emptyList()))
    val listSongsData: StateFlow<ListSongsData> = _listSongsData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getListSongsData(id: Long) {
        if (_loadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getListSongs( id.toString())
                Log.d("ListSongsViewModel", "获取成功 ${response.body()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _listSongsData.value = data
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
                    Log.e("ListSongsViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("ListSongsViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}