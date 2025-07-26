package com.example.module_login_register.viewModel

sealed class QrLoadState {
    object Init : QrLoadState()
    object Loading : QrLoadState()
    object Success : QrLoadState()
    data class Error(val message: String) : QrLoadState()
}