package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetWorkClient
import data.ListMusicData
import data.SingerMvData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingerMvViewModel: ViewModel() {
    val singerMvData = MutableLiveData<SingerMvData>()
    suspend fun getSingerMvData(id: Long) {
        withContext(Dispatchers.IO){
            try{
                val data = NetWorkClient.apiService10.getSingerMv(id)
                singerMvData.postValue(data)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }
}