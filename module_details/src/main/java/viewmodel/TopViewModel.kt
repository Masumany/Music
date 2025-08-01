import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetworkClient
import data.TopData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TopViewModel : ViewModel() {
    val topData = MutableLiveData<TopData?>()
    suspend fun getTopData(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                val response = NetworkClient.apiService.getSingerHotSongs(id)
                topData.postValue(response)
            } catch (e: Exception) {
                topData.postValue(null)
                e.printStackTrace()
            }
        }

    }
}