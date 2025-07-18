package com.example.module_login_register.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_login_register.bean.PassWordLoginData
import com.example.module_login_register.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordsLoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _passwordsLoginData = MutableStateFlow(PassWordLoginData(0, 0, "", ""))
    val passwordsLoginData: StateFlow<PassWordLoginData> = _passwordsLoginData.asStateFlow()

    fun loginWithPassword(phone: String, password: String) {
        if (phone.isEmpty() || password.isEmpty()){
            if (phone.isEmpty()){
                _loginState.value = LoginState.Error ("手机号不能为空")
                 return
            }
            if (password.isEmpty()){
                _loginState.value = LoginState.Error ("密码不能为空")
                 return
            }
        }
        if (phone.length != 11){
            _loginState.value = LoginState.Error("手机号长度有误")
            return
        }
        if (_loginState.value == LoginState.Loading){
            return
        }
            viewModelScope.launch {
                try {
                    _loginState.value = LoginState.Loading
                    val response = NetRepository.apiService.passwordsLogin(phone, password)
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null && data.code == 200) {
                            _passwordsLoginData.value = data
                            _loginState.value = LoginState.Success
                        }else{
                            if (data == null){
                                _loginState.value = LoginState.Error("数据为空 ${response.message()}")
                            }else{
                                _loginState.value = LoginState.Error("未知错误 ${response.message()}")
                            }
                        }
                    } else {
                        _loginState.value = LoginState.Error("请求失败 ${response.message()}")
                        Log.e("passwordViewModel", "getPasswordsLoginData : ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("PasswordLogin", "getPasswordsLoginData error: ${e.message}")
                    _loginState.value = LoginState.Error("登录异常 ${e.message.toString()}")
                    e.printStackTrace()
                }
            }
        }
    }