package com.example.module_recommened.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.NetworkClient
import com.example.lib.base.RecommenedData

class RecommenedViewModel : ViewModel() {
    val reData = MutableLiveData<RecommenedData>()
    suspend fun getRecommenedData(): RecommenedData {
        try {
            val result = NetworkClient.apiService.getRecommended()
            Log.d("TAG", "getRecommenedData: $result")
            return result
        } catch (e: Exception) {
            Log.d("TAG", "getRecommenedData: $e")
            return RecommenedData(0, 0, false, emptyList())
        }
    }
}