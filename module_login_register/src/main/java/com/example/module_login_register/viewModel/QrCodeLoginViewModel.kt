package com.example.module_login_register.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_login_register.bean.Data
import com.example.module_login_register.bean.DataX
import com.example.module_login_register.bean.QrCheckData
import com.example.module_login_register.bean.QrCreateData
import com.example.module_login_register.bean.QrLoginData
import com.example.module_login_register.repository.NetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrCodeLoginViewModel : ViewModel() {
    private val _QrCodeKey = MutableStateFlow(QrLoginData(0, Data(0, "")))
    val QrCodeKey: StateFlow<QrLoginData> = _QrCodeKey.asStateFlow()

    private val _QrCodeData = MutableStateFlow(QrCreateData(0, DataX("", "")))
    val QrCodeData: StateFlow<QrCreateData> = _QrCodeData.asStateFlow()

    private val _checkQrCodeData = MutableStateFlow(QrCheckData(0, "", ""))
    val checkQrCodeData: StateFlow<QrCheckData> = _checkQrCodeData.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private var checkQrJob: Job? = null

    fun getQrCodeKey() {
        if (_loginState.value is LoginState.Loading){
             return
        }
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val time = System.currentTimeMillis().toString()
                val response = NetRepository.apiService.getQRKey(time)
                Log.d("QrCodeLoginViewModel", "key获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _QrCodeKey.value = data
                        _loginState.value = LoginState.Success
                        createQrCode(data.data.unikey)
                    }else{
                        if (data == null){
                            _loginState.value = LoginState.Error("获取失败 ${response.message()}")
                        }else{
                            _loginState.value = LoginState.Error("未知错误 ${response.message()}")
                        }
                    }

                }else{
                    _loginState.value = LoginState.Error("key获取失败 ${response.message()}")
                    Log.e("QrCodeLoginViewModel", "key获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("QrCodeLoginViewModel", "key获取异常 ${e.message}")
                _loginState.value = LoginState.Error("key获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun createQrCode(key: String) {
        if (key.isEmpty()){
            _loginState.value = LoginState.Error("key为空")
            return
        }
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val response = NetRepository.apiService.createQR(key)
                if (response.isSuccessful){
                    Log.d("QrCodeLoginViewModel", "二维码创建成功 ${response.message()}")
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _QrCodeData.value = data
                        _loginState.value = LoginState.Success
                        checkQrCode(key)
                    }else{
                        if (data == null){
                            _loginState.value = LoginState.Error("创建二维码失败 ${response.message()}")
                        }else{
                            _loginState.value = LoginState.Error("未知错误 ${response.message()}")
                        }
                    }
                }else{
                    _loginState.value = LoginState.Error("请求失败 ${response.message()}")
                    Log.e("QrCodeLoginViewModel", "请求失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("QrCodeLoginViewModel", "请求异常 ${e.message}")
                _loginState.value = LoginState.Error("请求异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun checkQrCode(key: String) {
        checkQrJob?.cancel()
        checkQrJob = viewModelScope.launch {
            try {
                while (true){
                    val response = NetRepository.apiService.checkQR(key)
                    if (response.isSuccessful){
                        val data = response.body()
                        if (data != null ){
                            _checkQrCodeData.value = data
                            when(data.code){
                                800 ->{
                                    _loginState.value = LoginState.Loading
                                }
                                801 ->{
                                    _loginState.value = LoginState.Loading
                                }
                                802 ->{
                                    _loginState.value = LoginState.Success
                                    break
                                }
                                else ->{
                                    _loginState.value = LoginState.Error("二维码失效")
                                    Log.e("QrCodeLoginViewModel", "二维码失效: ${response.message()}")
                                     break
                                }
                            }
                        }else{
                            _loginState.value = LoginState.Error("二维码为空")
                        }
                    }else{
                        _loginState.value = LoginState.Error("请求失败 ${response.message()}")
                        Log.e("QrCodeLoginViewModel", "请求失败 ${response.message()}")
                    }
                }
            }catch (e: Exception){
                _loginState.value = LoginState.Error("请求异常 ${e.message}")
                Log.e("QrCodeLoginViewModel", "请求异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
}