package com.example.module_recommened

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.module_recommened.adapter.BannerAdapter
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.adapter.ReAdapter
import com.example.module_recommened.databinding.FragmentRecommendBinding

import com.example.module_recommened.viewmodel.BannerViewModel
import com.example.module_recommened.viewmodel.ListViewModel
import com.example.module_recommened.viewmodel.RecommenedViewModel
import kotlinx.coroutines.launch

class RecommendFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var rvlist: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var bannerViewModel: BannerViewModel
    private lateinit var recommendedViewModel: RecommenedViewModel
    private lateinit var listViewModel: ListViewModel

    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    private val songAdapter= LiAdapter()
    private var currentPage=1
    private val pageSize=5
    private var hasMoreData=true

    private var isLoading=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swipeRefresh
        rvlist = binding.rvlist
        viewPager = binding.viewPager
        recyclerView = binding.recyclerView

        rvlist.adapter=songAdapter
        val layoutManager=LinearLayoutManager(requireContext())
        rvlist.layoutManager = layoutManager
        rvlist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastPosition=layoutManager.findLastVisibleItemPosition()
                val totalItem=layoutManager.itemCount
                if (lastPosition >= totalItem - 1 && hasMoreData) {
                    lastPosition>=totalItem-1
                    loadNextPage()
                }
            }

            private fun loadNextPage() {
                if (isLoading) return
                isLoading = true
                currentPage ++
                fetchListData(currentPage)
            }
        })


        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // 初始化 ViewModel
        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        recommendedViewModel = ViewModelProvider(this)[RecommenedViewModel::class.java]
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]

        loadFirstPage()
        loadData()

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                refreshData()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun refreshData() {
        currentPage = 1
        lifecycleScope.launch {
            loadData()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadFirstPage() {
        currentPage=1
        fetchListData(currentPage)
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
                    loadFirstPage()
                }

                bannerDeferred.join()
                recommendedDeferred.join()
                listDeferred.join()
            }
        } catch (e: Exception) {
            Log.e("RecommendFragment", "Error fetching data: ${e.message}")
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
            if (result != null) {
                if (result.isNotEmpty()) {
                    recyclerView.adapter = ReAdapter(result)
                }
            }
        } catch (e: Exception) {
            Log.e("RecommendedData", "Error fetching recommended data: ${e.message}")
        }
    }

    private fun fetchListData(page:Int) {
        lifecycleScope.launch {
            try {
                val result = listViewModel.getListData(page,pageSize)
                Log.d("ListData", "原始响应: $result")

                if (result.code == 200 && result != null) {
                    val songs=result.data?.dailySongs
                    hasMoreData= songs?.size!! >=pageSize

                    if(page==1){
                        songAdapter.submitList(songs)
                    }else{
                        songAdapter.addMoreData(songs)
                    }
                } else {
                    Log.e("ListData", "数据为空: ${result.code}")
                    hasMoreData=false
                }
            } catch (e: Exception) {
                Log.e("ListData", "错误: ${e.message}")
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlay()
        _binding = null
    }

    private fun stopPlay() {
        isPlaying = false
        handler.removeCallbacksAndMessages(null)
    }
}