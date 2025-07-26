package com.example.module_musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lib.base.NetworkClient
import data.CommentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentViewModel : ViewModel() {
    val hotComments = MutableLiveData<List<CommentData.HotComment>>()
    val errorMsg = MutableLiveData<String>()

    // 获取评论数据
    fun fetchCommentData(songId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.apiService.getComment(songId)
                }
                if (response.code == 200) {
                    Log.d("CommentVM", "热门评论数量：${response.hotComments.size}")
                    hotComments.value = response.hotComments
                } else {
                    errorMsg.value = "接口返回错误：${response.code}"
                }
            } catch (e: Exception) {
                Log.e("CommentVM", "请求失败", e)
                errorMsg.value = "网络错误：${e.message}"
            }
        }
    }
}
