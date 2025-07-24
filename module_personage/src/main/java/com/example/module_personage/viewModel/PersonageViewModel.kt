package com.example.module_personage.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_personage.bean.user.Userdetail
import com.example.module_personage.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonageViewModel:ViewModel () {
    private val _userDetailData = MutableStateFlow<Userdetail?>(null)
    val userDetailData: StateFlow<Userdetail?> = _userDetailData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun getUserDetail(id: Int) {
        if (_loadState.value is LoadState.Loading){
             return
        }
        viewModelScope.launch {
            try{
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getUserDetail(id)
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _userDetailData.value = data
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
                    Log.e("PersonageViewModel", "获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("PersonageViewModel", "获取异常 ${e.message}")
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}