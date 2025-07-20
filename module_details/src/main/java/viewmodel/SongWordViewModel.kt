package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lib.base.NetWorkClient
import data.SongWordData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongWordViewModel : ViewModel() {
    // 歌词数据（对外暴露不可变LiveData，避免外部直接修改）
    private val _songWordData = MutableLiveData<SongWordData?>()
    val songWordData = _songWordData

    // 加载状态（用于UI显示加载中/失败状态）
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState = _loadingState

    // 加载状态密封类（更规范的状态管理）
    sealed class LoadingState {
        object Loading : LoadingState() // 加载中
        object Success : LoadingState() // 成功
        data class Error(val message: String) : LoadingState() // 错误
    }

    /**
     * 获取歌词数据
     * @param id 歌曲ID（Long类型）
     */
    fun fetchSongWordData(id: Long) {
        // 启动加载状态
        _loadingState.value = LoadingState.Loading

        // 使用viewModelScope，确保协程在ViewModel销毁时自动取消
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 网络请求在IO线程执行
                val response = NetWorkClient.apiService6.getSongWord(id.toString())

                // 切换到主线程更新数据
                withContext(Dispatchers.Main) {
                    if (response.code == 200) {
                        _songWordData.value = response
                        _loadingState.value = LoadingState.Success
                    } else {
                        // 接口返回错误码（如404、500）
                        _loadingState.value = LoadingState.Error("接口错误：${response.code}")
                        _songWordData.value = null
                    }
                }
            } catch (e: Exception) {
                // 捕获网络异常、解析异常等
                withContext(Dispatchers.Main) {
                    _loadingState.value = LoadingState.Error(e.message ?: "获取歌词失败")
                    _songWordData.value = null
                }
            }
        }
    }
}