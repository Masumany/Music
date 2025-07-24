package com.example.module_hot.viewModel

import com.example.module_hot.bean.list.ListItem

sealed class LoadState {
    object Init : LoadState()
    object Loading : LoadState() // 加载中
    object Success : LoadState()
    data class Error(val message: String) : LoadState() // 失败（携带错误信息）
}