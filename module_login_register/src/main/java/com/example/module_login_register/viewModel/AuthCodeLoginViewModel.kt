package com.example.module_login_register.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_login_register.bean.AuthVerifyData
import com.example.module_login_register.bean.SendData
import com.example.module_login_register.repository.NetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthCodeLoginViewModel : ViewModel() {
    private val _sendAuthCodeData = MutableStateFlow(SendData(0, false))
    val sendAuthCodeData: StateFlow<SendData> = _sendAuthCodeData.asStateFlow()

    private val _verifyAuthData = MutableStateFlow(AuthVerifyData(0, false))
    val verifyAuthData: StateFlow<AuthVerifyData> = _verifyAuthData.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(0)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()
    private var countdownJob: Job? = null

    fun sendAuthCode(phone: String) {
        if (phone.isEmpty()){
            _loginState.value = LoginState.Error("手机号不能为空")
        }
        if (phone.length != 11){
            _loginState.value = LoginState.Error("手机号长度有误")
        }
        if (_loginState.value == LoginState.Loading){
             return
        }
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val response = NetRepository.apiService.getAuth(phone)
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _sendAuthCodeData.value = data
                        _loginState.value = LoginState.Success
                        startCountdown()
                    }else{
                        if (data == null){
                            _loginState.value = LoginState.Error("获取失败 ${response.message()}")
                        }else{
                            _loginState.value = LoginState.Error("未知错误 ${response.message()}")
                        }
                    }
                }else {
                    _loginState.value = LoginState.Error("请求失败 ${response.message()}")
                    Log.e("AuthCodeViewModel", "getAuthCodeData : ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("AuthCodeViewModel", "getAuthCodeData Error: ${e.message}")
                _loginState.value = LoginState.Error("登录异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun startCountdown(total : Int = 60) {
        countdownJob?.cancel()
        _countdownSeconds.value = total
        countdownJob = viewModelScope.launch {
            repeat( total){
                delay(1000)
                _countdownSeconds.value = _countdownSeconds.value!! - 1
                if (_countdownSeconds.value == 0) {
                    countdownJob?.cancel()
                }
            }
            _countdownSeconds.value = 0
        }
    }
    fun verifyAuthCode(phone: String, captcha: String) {
        if (phone.isEmpty()){
            _loginState.value = LoginState.Error("手机号不能为空")
        }
        if (phone.length != 11){
            _loginState.value = LoginState.Error("手机号长度有误")
        }
        if (captcha.isEmpty()){
            _loginState.value = LoginState.Error("验证码不能为空")
        }
        if (_loginState.value == LoginState.Loading){
            return
        }
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val response = NetRepository.apiService.verifyAuth(phone, captcha)
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _verifyAuthData.value = data
                        _loginState.value = LoginState.Success
                    }else{
                        if (data == null){
                            _loginState.value = LoginState.Error("数据为空 ${response.message()}")
                        } else{
                            _loginState.value = LoginState.Error("未知错误 ${response.message()}")
                        }
                    }
                }else {
                    _loginState.value = LoginState.Error("请求失败 ${response.message()}")
                    Log.e("AuthCodeViewModel", "verifyAuthCodeData : ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("AuthCodeViewModel", "verifyAuthCodeData Error: ${e.message}")
                _loginState.value = LoginState.Error("登录异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}