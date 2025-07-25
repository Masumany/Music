package com.example.module_login_register.viewModel

sealed class LoginState {
    object Success : LoginState()
    object Loading : LoginState()
    object Init : LoginState()
    data class Error(val message: String) : LoginState()
}