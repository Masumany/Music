import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetworkClient
import data.ListMusicData

class SongViewModel : ViewModel() {
    val listMusicData = MutableLiveData<ListMusicData>()

    suspend fun getListMusicData(id: Long) { // 接收从Activity传来的id
        try {
            Log.d("SongViewModel", "开始请求歌曲列表，id=$id")
            val response = NetworkClient.apiService.getPlayList(id.toString())
            Log.d("SongViewModel", "接口返回：code=${response.code}，歌曲数=${response.songs.size}")
            listMusicData.postValue(response)
        } catch (e: Exception) {
            Log.e("SongViewModel", "请求失败：${e.message}", e)
            listMusicData.postValue(
                ListMusicData(
                    code = -1, songs = emptyList(),
                    privileges = emptyList()
                )
            )
        }
    }
}