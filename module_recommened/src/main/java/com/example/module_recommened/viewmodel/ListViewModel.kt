package com.example.module_recommened.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.ListData
import com.example.lib.base.NetWorkClient

class ListViewModel : ViewModel(){
    val listData = MutableLiveData<ListData>()
    suspend fun getListData(page: Int, pageSize: Int): ListData {
        try {
            val result = NetWorkClient.apiService2.getListData()
            return result
        } catch (e: Exception) {
            Log.d("TAG", "getRecommenedData: $e")
            return ListData(code = 500, data = null)
        }
    }
}