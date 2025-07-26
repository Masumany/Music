package com.example.module_login_register.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.module_login_register.bean.VisitorLoginData
import com.example.module_login_register.repository.NetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _loadingState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState : StateFlow<LoginState> = _loadingState.asStateFlow()

    private val _visitorLoginData = MutableStateFlow(VisitorLoginData(0, "", 0, 0))
    val visitorLoginData : StateFlow<VisitorLoginData> = _visitorLoginData.asStateFlow()

    private val viewModelStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun getVisitorLoginData() {
        if (_loadingState.value != LoginState.Loading) {
            viewModelStore.launch {
                try {
                    _loadingState.value = LoginState.Loading
                    val response = NetRepository.apiService.visitorLogin()
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null && data.code == 200) {
                            _visitorLoginData.value = data
                            _loadingState.value = LoginState.Success
                        } else{
                            if (data == null){
                                _loadingState.value = LoginState.Error("数据为空 ${response.message()}")
                            }else{
                                _loadingState.value = LoginState.Error("未知错误 ${response.message()}")
                            }
                        }
                    } else {
                        _loadingState.value = LoginState.Error("请求失败 ${response.message()}")
                        Log.e("LoginViewModel", "getVisitorLoginData error: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "getVisitorLoginData  error: ${e.message}")
                    _loadingState.value = LoginState.Error("登录异常 ${e.message.toString()}")
                    e.printStackTrace()
                }
            }
        }else {
            Log.e("LoginViewModel", "getVisitorLoginData: 请稍后再试")
            return
        }
    }
}