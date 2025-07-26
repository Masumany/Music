package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetworkClient
import data.SingerMvData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingerMvViewModel : ViewModel() {
    val singerMvData = MutableLiveData<SingerMvData>()
    suspend fun getSingerMvData(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                val data = NetworkClient.apiService.getSingerMvs(id)
                singerMvData.postValue(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}