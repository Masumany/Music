//package com.example.moudle_search.viewModel
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.moudle_search.bean.ListResult
//import com.example.moudle_search.bean.ListsResultData
//import com.example.moudle_search.bean.SingerResult
//import com.example.moudle_search.repository.NetRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class SongListViewModel : ViewModel() {
//
//    private val _listsResult = MutableStateFlow(ListsResultData(0, ListResult(0, emptyList()) ))
//    val listsResult: StateFlow<ListsResultData> = _listsResult.asStateFlow()
//
//    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
//    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
//
//    fun getListsData(keywords: String) {
//        if (_loadState.value is LoadState.Loading){
//            return
//        }
//        viewModelScope.launch {
//            try {
//                _loadState.value = LoadState.Loading
//                val response = NetRepository.apiService.getListsResult( keywords)
//                Log.d("SingerViewModel", "获取成功 ${response.message()}")
//                if (response.isSuccessful){
//                    val data = response.body()
//                    if (data != null && data.code == 200){
//                        _listsResult.value = data
//                        _loadState.value = LoadState.Success
//                    }else{
//                        if (data == null){
//                            _loadState.value = LoadState.Error("获取失败 ${response.message()}")
//                        }else{
//                            _loadState.value = LoadState.Error("未知错误 ${response.message()}")
//                        }
//                    }
//
//                }else{
//                    _loadState.value = LoadState.Error("获取失败 ${response.message()}")
//                    Log.e("SingerViewModel", "获取失败 ${response.message()}")
//                }
//            }catch (e: Exception){
//                Log.e("SingerViewModel", "获取异常 ${e.message}")
//                _loadState.value = LoadState.Error("获取异常 ${e.message}")
//                e.printStackTrace()
//            }
//        }
//    }
//}