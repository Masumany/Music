package viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetWorkClient
import data.ArtistDetailData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingerHomeViewModel : ViewModel() {
    // 暴露给UI的LiveData（对外只读）
    val singerHomeData = MutableLiveData<ArtistDetailData?>()

    // 加载歌手数据
    suspend fun getSingerHomeData(singerId: Long) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SingerVM", "请求歌手ID: $singerId 的数据")
                val response = NetWorkClient.apiService7.getArtistDetail(singerId)
                Log.d("SingerVM", "接口返回code: ${response.code}")
                singerHomeData.postValue(response) // 子线程用postValue
            } catch (e: Exception) {
                Log.e("SingerVM", "请求失败", e)
                singerHomeData.postValue(null) // 异常时发送null
            }
        }
    }
}
