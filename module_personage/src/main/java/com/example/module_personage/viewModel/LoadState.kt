package com.example.module_personage.viewModel

sealed class LoadState {
    object Loading : LoadState()
    object Success : LoadState()
    object Init : LoadState()
    data class Error(val message: String) : LoadState()
}