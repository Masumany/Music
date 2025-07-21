package com.example.module_mvplayer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_mvplayer.repositorty.NetRepository
import com.example.module_mvplayer.bean.commentData.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MvCommentViewModel: ViewModel() {
    private val _commentList = MutableStateFlow<List<Comment>>(emptyList())
    val commentList: StateFlow<List<Comment>> = _commentList.asStateFlow()

    private val _commentCount = MutableStateFlow(0)
    val commentCount: StateFlow<Int> = _commentCount.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private val offset = currentPage * pageSize
    private var hasMore = true

    fun loadComments(mvId: String) {
        _loadState.value = LoadState.Loading
        currentPage = 0
        if (_loadState.value is LoadState.Loading) {
            return
        }

        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getMvComment(mvId, offset, pageSize)
                if (response.isSuccessful) {
                    val comments = response.body()?.comments ?: emptyList()
                    _commentList.value = comments
                    _commentCount.value = response.body()?.total ?: 0
                    _loadState.value = LoadState.Success
                } else {
                    _loadState.value = LoadState.Error("请求失败${response.code()}")
                }
            }catch (e: Exception){
                _loadState.value = LoadState.Error("请求失败")
            }
        }
    }
}