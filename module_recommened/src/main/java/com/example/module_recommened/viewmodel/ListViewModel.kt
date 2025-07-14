package com.example.module_recommened.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.module_recommened.api.NetWorkClient
import com.example.module_recommened.model.ListData

class ListViewModel : ViewModel(){
    val listData = MutableLiveData<ListData>()
    suspend fun getListData(): ListData {
        try {
            val result = NetWorkClient.apiService2.getListData()
            return result
        } catch (e: Exception) {
            Log.d("TAG", "getRecommenedData: $e")
            return ListData(code = 500, data = null)
        }
    }
}