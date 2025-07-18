package com.example.module_musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lib.base.NetWorkClient
import data.CommentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CommentViewModel : ViewModel() {
    // 热门评论数据（与你的Adapter中的HotComment类型对应）
    val hotComments = MutableLiveData<List<CommentData.HotComment>>()
    // 错误信息
    val errorMsg = MutableLiveData<String>()

    // 获取评论数据（与Fragment中的调用对应）
    fun fetchCommentData(songId: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetWorkClient.apiService5.getComment(songId)
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
