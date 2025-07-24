package com.example.module_login_register.viewModel

import android.content.SharedPreferences
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrCodeLoginViewModel(private  val sharedPreferences : SharedPreferences) : ViewModel() {
    private val _QrCodeKey = MutableStateFlow(QrLoginData(0, Data(0, "")))
    val QrCodeKey: StateFlow<QrLoginData> = _QrCodeKey.asStateFlow()

    private val _QrCodeData = MutableStateFlow(QrCreateData(0, DataX("", "")))
    val QrCodeData: StateFlow<QrCreateData> = _QrCodeData.asStateFlow()

    private val _checkQrCodeData = MutableStateFlow(QrCheckData(0, "", ""))
    val checkQrCodeData: StateFlow<QrCheckData> = _checkQrCodeData.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _qrState = MutableStateFlow<QrLoadState>(QrLoadState.Init)
    val qrState: StateFlow<QrLoadState> = _qrState.asStateFlow()

    private var checkQrJob: Job? = null

    fun getQrCodeKey() {
        if (_qrState.value is QrLoadState.Loading){
             return
        }
        viewModelScope.launch {
            try {
                _qrState.value = QrLoadState.Loading
                val time = System.currentTimeMillis().toString()
                val response = NetRepository.apiService.getQRKey(time)
                Log.d("QrCodeLoginViewModel", "key获取成功 ${response.message()}")
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _QrCodeKey.value = data
                        _qrState.value = QrLoadState.Success
                        createQrCode(data.data.unikey, qrimg = "true")
                    }else{
                        if (data == null){
                            _qrState.value = QrLoadState.Error("获取失败 ${response.message()}")
                        }else{
                            _qrState.value = QrLoadState.Error("未知错误 ${response.message()}")
                        }
                    }

                }else{
                    _qrState.value = QrLoadState.Error("key获取失败 ${response.message()}")
                    Log.e("QrCodeLoginViewModel", "key获取失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("QrCodeLoginViewModel", "key获取异常 ${e.message}")
                _qrState.value = QrLoadState.Error("key获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun createQrCode(key: String,qrimg: String = "true") {
        if (key.isEmpty()){
            _qrState.value = QrLoadState.Error("key为空")
            return
        }
        viewModelScope.launch {
            try {
                _qrState.value = QrLoadState.Loading
                val response = NetRepository.apiService.createQR(key,qrimg)
                if (response.isSuccessful){
                    Log.d("QrCodeLoginViewModel", "二维码创建成功 ${response.message()}")
                    val data = response.body()
                    if (data != null && data.code == 200){
                        _QrCodeData.value = data
                        _qrState.value = QrLoadState.Success
                        Log.d("QrCodeLoginViewModel", "二维码地址: ${data.data.qrimg}")
                        checkQrCode(key)
                    }else{
                        if (data == null){
                            _qrState.value = QrLoadState.Error("创建二维码失败 ${response.message()}")
                        }else{
                            _qrState.value = QrLoadState.Error("未知错误 ${response.message()}")
                        }
                    }
                }else{
                    _qrState.value = QrLoadState.Error("请求失败 ${response.message()}")
                    Log.e("QrCodeLoginViewModel", "请求失败 ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("QrCodeLoginViewModel", "请求异常 ${e.message}")
                _qrState.value = QrLoadState.Error("请求异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun checkQrCode(key: String,noCookie : String = "faulse") {
        checkQrJob?.cancel()
        checkQrJob = viewModelScope.launch {
            try {
                while (true){
                    val response = NetRepository.apiService.checkQR(key,noCookie)
                    delay(2000)
                    if (response.isSuccessful){
                        val qrData = response.body()
                        if (qrData != null ){
                            _checkQrCodeData.value = qrData
                            when(qrData.code){
                                800 ->{
                                    _loginState.value = LoginState.Error("二维码已过期")
                                    break
                                }
                                801 ->{
                                    _qrState.value = QrLoadState.Success
                                    delay(2000)
                                }
                                802 ->{
                                    _loginState.value = LoginState.Loading
                                    delay(2000)
                                }
                                803 ->{
                                    _loginState.value = LoginState.Success
                                    viewModelScope.launch {
                                        val userId = checkLoginStatus()
                                        if (userId != null) {
                                            sharedPreferences.edit()
                                                .putString("cookie", qrData.cookie)
                                                .putString("userId", userId.toString())
                                                .putBoolean("isLogin", true)
                                                .apply()
                                        } else {
                                            _loginState.value = LoginState.Error("登录状态校验失败,请重试")
                                        }
                                    }
                                    break
                                }
                                502 -> {
                                    // 加 noCookie 参数重新请求
                                    val newResponse = NetRepository.apiService.checkQR(key, noCookie)
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
    fun checkLoginStatus(): String?{
        var userId: String? = null
        viewModelScope.launch {
            try {
                val response = NetRepository.apiService.checkLoginStatus()
                if (response.isSuccessful){
                    val loginStatus = response.body()
                    if (loginStatus != null){
                        if (loginStatus.data.code == 200){
                            userId = loginStatus.data.account.id.toString()
                            _loginState.value = LoginState.Success
                        }else{
                            _loginState.value = LoginState.Error("登录失败 ${loginStatus.data.code}")
                        }
                    }else{
                        _loginState.value = LoginState.Error("登录失败 ${response.message()}")
                    }
                }
            }catch (e: Exception){
                _loginState.value = LoginState.Error("登录失败 ${e.message}")
            }
        }
        return userId
    }
    override fun onCleared() {
        super.onCleared()
        checkQrJob?.cancel()
    }
}