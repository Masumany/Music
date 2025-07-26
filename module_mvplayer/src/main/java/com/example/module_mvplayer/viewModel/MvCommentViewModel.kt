package com.example.module_mvplayer.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_mvplayer.repositorty.NetRepository
import com.example.module_mvplayer.bean.commentData.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MvCommentViewModel : ViewModel() {
    private val _commentList = MutableStateFlow<List<Comment>>(emptyList())
    val commentList: StateFlow<List<Comment>> = _commentList.asStateFlow()

    // 评论总数数据流
    private val _commentCount = MutableStateFlow(0)
    val commentCount: StateFlow<Int> = _commentCount.asStateFlow()

    // 加载状态数据流
    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    // 分页参数（当前页、页大小）
    private var currentPage = 0
    private val pageSize = 20
    private var hasMore = true  // 是否有更多数据

    fun loadComments(mvId: String, isRefresh: Boolean = true) {
        // 拦截重复请求：若正在加载，直接返回
        if (_loadState.value is LoadState.Loading) {
            Log.d("MvCommentViewModel", "正在加载中，拦截重复请求")
            return
        }

        // 刷新时重置参数
        if (isRefresh) {
            currentPage = 0
            hasMore = true
        }

        // 没有更多数据时，直接返回（仅加载更多时生效）
        if (!isRefresh && !hasMore) {
            _loadState.value = LoadState.Success
            return
        }

        // 启动协程请求数据（viewModelScope 会在 ViewModel 销毁时自动取消）
        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                // 动态计算 offset（用当前 currentPage 计算）
                val offset = currentPage * pageSize
                val response = NetRepository.apiService.getMvComment(mvId, offset, pageSize)

                if (response.isSuccessful) {
                    val body = response.body()
                    val newComments = body?.comments ?: emptyList()
                    val totalCount = body?.total ?: 0

                    // 刷新：直接替换数据；加载更多：拼接数据
                    _commentList.value = if (isRefresh) {
                        newComments
                    } else {
                        _commentList.value + newComments
                    }
                    _commentCount.value = totalCount

                    // 判断是否有更多数据（当前加载的数量 < 总数，且当前页数据满页）
                    hasMore = _commentList.value.size < totalCount && newComments.size == pageSize
                    currentPage++  // 页码+1（下次加载更多用）
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("请求失败：${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loadState.value = LoadState.Error("请求失败：${e.message ?: "网络异常"}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("MvCommentViewModel", "ViewModel 已销毁")
    }
}