package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lib.base.NetworkClient
import data.SongWordData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongWordViewModel : ViewModel() {
    private val _songWordData = MutableLiveData<SongWordData?>()
    val songWordData = _songWordData

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState = _loadingState

    sealed class LoadingState {
        object Loading : LoadingState() // 加载中
        object Success : LoadingState() // 成功
        data class Error(val message: String) : LoadingState() // 错误
    }

    fun fetchSongWordData(id: Long) {
        _loadingState.value = LoadingState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 网络请求在IO线程执行
                val response = NetworkClient.apiService.getSongLyric(id.toString())

                // 切换到主线程更新数据
                withContext(Dispatchers.Main) {
                    if (response.code == 200) {
                        _songWordData.value = response
                        _loadingState.value = LoadingState.Success
                    } else {
                        // 接口返回错误码
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