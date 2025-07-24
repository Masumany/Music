package com.example.module_mvplayer.viewModel

sealed class LoadState {
    object Success : LoadState()
    object Loading : LoadState()
    object Init : LoadState()
    data class Error(val message: String) : LoadState()
}