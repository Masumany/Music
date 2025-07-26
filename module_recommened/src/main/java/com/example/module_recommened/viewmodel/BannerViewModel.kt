package com.example.module_recommened.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lib.base.BannerData
import com.example.lib.base.NetworkClient

class BannerViewModel : ViewModel() {
    val bannerData = MutableLiveData<BannerData>()
    suspend fun getBannerData(): Result<BannerData> {
        try {
            val response = NetworkClient.apiService.getBanner()
            Log.d("BannerViewModel", "getBannerData: $response")
            return Result.success(response)
        } catch (e: Exception) {
            Log.d("BannerViewModel", "getBannerData: $e")
            return Result.failure(e)
        }
    }
}