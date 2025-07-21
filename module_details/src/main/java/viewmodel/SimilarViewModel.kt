package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetWorkClient
import data.SimilarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimilarViewModel:ViewModel () {
    val similarData = MutableLiveData<SimilarData?>()

    suspend fun getSimilarData(id: Long) {
        withContext(Dispatchers.IO){
            try{
                val response = NetWorkClient.apiService8.getSimilar(id)
                similarData.postValue( response)
            }catch (e:Exception){
                similarData.postValue(null)
                e.printStackTrace()
            }
        }

    }
}