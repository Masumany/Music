package com.example.module_recommened

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.module_recommened.viewmodel.BannerViewModel
import com.example.module_recommened.viewmodel.ListViewModel
import com.example.module_recommened.viewmodel.RecommenedViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var bannerViewModel: BannerViewModel
    private lateinit var recommendedViewModel: RecommenedViewModel
    private lateinit var listViewModel: ListViewModel
    private lateinit var rvlist:RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout=findViewById(R.id.swipeRefresh)

        rvlist=findViewById(R.id.rvlist)
        rvlist.layoutManager = LinearLayoutManager(this)

        listViewModel= ViewModelProvider(this)[ListViewModel::class.java]
        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        recommendedViewModel = ViewModelProvider(this)[RecommenedViewModel::class.java]

        viewPager = findViewById(R.id.viewPager)
        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        loadData()
        swipeRefreshLayout.setOnRefreshListener {
           lifecycleScope.launch {
               loadData()
               swipeRefreshLayout.isRefreshing = false
           }
        }
    }

    private fun loadData() {
        try {
            lifecycleScope.launch {
            val bannerDeferred = lifecycleScope.launch {
                fetchBannerData()
            }

            val recommendedDeferred = lifecycleScope.launch {
                fetchRecommendedData()
            }
            val listDeferred = lifecycleScope.launch {
                fetchListData()
            }
            bannerDeferred.join()
            recommendedDeferred.join()
            listDeferred.join()
        }
        }catch (e: Exception){
            Log.e("MainActivity", "Error fetching data: ${e.message}")
        }


    }


    private suspend fun fetchBannerData() {
        try {
            val result = bannerViewModel.getBannerData()
            if (result.isSuccess) {
                result.getOrNull()?.banners?.let { banners ->
                    viewPager.adapter = BannerAdapter(banners)
                    startPlay()
                }
            }
        } catch (e: Exception) {
          Log.e("BannerData", "Error fetching banner data: ${e.message}")
        }
    }

    private suspend fun fetchRecommendedData() {
        try {
            val result = recommendedViewModel.getRecommenedData().result
            if (result.isNotEmpty()) {
                recyclerView.adapter = ReAdapter(result)
            }

        } catch (e: Exception) {
            Log.e("RecommendedData", "Error fetching recommended data: ${e.message}")
        }
    }
    suspend fun fetchListData() {
        try {
            val result = listViewModel.getListData()
            Log.d("ListData", "原始响应: $result") // 检查实际返回结构

            if (result.code == 200 && result != null) {
                rvlist.adapter = LiAdapter(result)
            } else {
                Log.e("ListData", "数据为空: ${result.code}")
            }
        } catch (e: Exception) {
            Log.e("ListData", "错误: ${e.message}")
        }
    }


    private fun startPlay() {
        if (!isPlaying && viewPager.adapter != null) {
            isPlaying = true
            val runnable = object : Runnable {
                override fun run() {
                    if (viewPager.adapter != null) {
                        val itemCount = viewPager.adapter!!.itemCount
                        if (itemCount > 1) {
                            val nextItem = (viewPager.currentItem + 1) % itemCount
                            viewPager.setCurrentItem(nextItem, true)
                        }
                    }
                    if (isPlaying) {
                        handler.postDelayed(this, 3000)
                    }
                }
            }
            handler.postDelayed(runnable, 3000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
    }

    private fun stopPlay() {
        isPlaying = false
        handler.removeCallbacksAndMessages(null)
    }
}