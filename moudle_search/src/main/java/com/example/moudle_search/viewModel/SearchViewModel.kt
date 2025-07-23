package com.example.moudle_search.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moudle_search.bean.searchHot.Result
import com.example.moudle_search.bean.searchHot.SearchHotData
import com.example.moudle_search.bean.searchKeyWord.Data
import com.example.moudle_search.bean.searchKeyWord.SearchKeyWordData
import com.example.moudle_search.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private val _hotData = MutableStateFlow(SearchHotData(0, Result(emptyList())))
    val hotData: StateFlow<SearchHotData> = _hotData.asStateFlow()

    private val _keyWord = MutableStateFlow(SearchKeyWordData(0, Data("",0, "")))
    val keyWord: StateFlow<SearchKeyWordData> = _keyWord.asStateFlow()

    private val _keyWordLoadState = MutableStateFlow<LoadState>(LoadState.Init)
    val keyWordLoadState: StateFlow<LoadState> = _keyWordLoadState.asStateFlow()

    private val _hotLoadState = MutableStateFlow<LoadState>(LoadState.Init)
    val hotLoadState: StateFlow<LoadState> = _hotLoadState.asStateFlow()

    fun getHotData() {
        if (_hotLoadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _hotLoadState.value = LoadState.Loading
                val response = NetRepository.apiService.getHotSearch()
                Log.d("SearchHot", "获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _hotData.value = data
                        _hotLoadState.value = LoadState.Success
                    }else{
                        if (data == null){
                            _hotLoadState.value = LoadState.Error("获取失败 ${response.message()}")
                        }else{
                            _hotLoadState.value = LoadState.Error("未知错误 ${response.message()}")
                        }
                    }

                }else{
                    _hotLoadState.value = LoadState.Error("获取失败 ${response.message()}")
                    Log.e("SearchHot", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("SearchHot", "获取异常 ${e.message}")
                _hotLoadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun getKeyWord() {
        if (_keyWordLoadState.value is LoadState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _keyWordLoadState.value = LoadState.Loading
                val response = NetRepository.apiService.getSearchKeyWord()
                Log.d("KeyWord", "获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _keyWord.value = data
                        _keyWordLoadState.value = LoadState.Success
                    }else{
                        if (data == null){
                            _keyWordLoadState.value = LoadState.Error("获取失败 ${response.message()}")
                        }else{
                            _keyWordLoadState.value = LoadState.Error("未知错误 ${response.message()}")
                        }
                    }

                }else{
                    _keyWordLoadState.value = LoadState.Error("获取失败 ${response.message()}")
                    Log.e("KeyWord", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("KeyWord", "获取异常 ${e.message}")
                _keyWordLoadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}