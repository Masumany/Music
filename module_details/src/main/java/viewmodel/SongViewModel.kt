import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetWorkClient
import data.ListMusicData

// 修改 SongViewModel
class SongViewModel : ViewModel() {
    val listMusicData = MutableLiveData<ListMusicData>()

    // 新增 id 参数，用于接口请求
    suspend fun getListMusicData(id: Long) { // 接收从Activity传来的id
        try {
            Log.d("SongViewModel", "开始请求歌曲列表，id=$id")
            val response = NetWorkClient.apiService4.getPlayList(id.toString())
            Log.d("SongViewModel", "接口返回：code=${response.code}，歌曲数=${response.songs.size}")
            listMusicData.postValue(response)
        } catch (e: Exception) {
            Log.e("SongViewModel", "请求失败：${e.message}", e)
            listMusicData.postValue(ListMusicData(
                code = -1, songs = emptyList(),
                privileges = emptyList()
            ))
        }
    }
}