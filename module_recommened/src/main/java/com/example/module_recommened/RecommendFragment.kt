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
import com.example.lib.base.Song as BaseSong
import com.example.module_recommened.adapter.BannerAdapter
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.adapter.ReAdapter
import com.example.module_recommened.databinding.FragmentRecommendBinding
import com.example.module_recommened.viewmodel.BannerViewModel
import com.example.module_recommened.viewmodel.ListViewModel
import com.example.module_recommened.viewmodel.RecommenedViewModel
import com.example.yourproject.converter.DataConverter
import data.ListMusicData
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
    private val songAdapter = LiAdapter()
    private var currentPage = 1
    private val pageSize = 5
    private var hasMoreData = true
    private var isLoading = false

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

        rvlist.adapter = songAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        rvlist.layoutManager = layoutManager

        rvlist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return // 只处理向下滚动

                val lastPosition = layoutManager.findLastVisibleItemPosition()
                val totalItem = layoutManager.itemCount


                if (lastPosition >= totalItem - 1 && hasMoreData && !isLoading) {
                    loadNextPage()
                }
            }
        })


        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )


        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        recommendedViewModel = ViewModelProvider(this)[RecommenedViewModel::class.java]
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]

        // 加载数据
        loadData()

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                refreshData()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    // 刷新数据
    private fun refreshData() {
        currentPage = 1
        lifecycleScope.launch {
            loadFirstPage()
        }
    }

    // 加载第一页
    private fun loadFirstPage() {
        currentPage = 1
        fetchListData(currentPage)
    }

    // 加载下一页
    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true
        currentPage++
        fetchListData(currentPage)
    }

    // 加载所有数据
    private fun loadData() {
        lifecycleScope.launch {
            try {
                // 并行加载不同数据
                launch { fetchBannerData() }
                launch { fetchRecommendedData() }
                launch { loadFirstPage() }
            } catch (e: Exception) {
                Log.e("RecommendFragment", "加载数据失败: ${e.message}")
            }
        }
    }

    // 获取轮播图数据
    private suspend fun fetchBannerData() {
        try {
            val result = bannerViewModel.getBannerData()
            if (result.isSuccess) {
                result.getOrNull()?.banners?.let { banners ->
                    viewPager.adapter = BannerAdapter(banners)
                    startPlay() // 启动轮播
                }
            }
        } catch (e: Exception) {
            Log.e("BannerData", "轮播图加载失败: ${e.message}")
        }
    }

    // 获取推荐数据
    private suspend fun fetchRecommendedData() {
        try {
            val result = recommendedViewModel.getRecommenedData().result
            if (!result.isNullOrEmpty()) {
                recyclerView.adapter = ReAdapter(result)
            }
        } catch (e: Exception) {
            Log.e("RecommendedData", "推荐数据加载失败: ${e.message}")
        }
    }

    private fun fetchListData(page: Int) {
        lifecycleScope.launch {
            try {
                val result = listViewModel.getListData(page, pageSize)
                Log.d("ListData", "响应码: ${result.code}")

                if (result.code == 200) {
                    val originalSongs: List<BaseSong> = result.data?.dailySongs ?: emptyList()
                    val convertedSongs = DataConverter.convertBaseSongList(originalSongs)
                    hasMoreData = convertedSongs.size >= pageSize
                    if (page == 1) {
                        songAdapter.submitList(convertedSongs)
                    } else {
                        songAdapter.addMoreData(convertedSongs)
                    }
                } else {
                    hasMoreData = false
                    Log.e("ListData", "数据错误: ${result.code}")
                }
            } catch (e: Exception) {
                Log.e("ListData", "加载失败: ${e.message}")
            } finally {
                isLoading = false // 无论成功失败都标记为未加载
            }
        }
    }

    // 启动轮播
    private fun startPlay() {
        if (!isPlaying && viewPager.adapter != null) {
            isPlaying = true
            val runnable = object : Runnable {
                override fun run() {
                    val itemCount = viewPager.adapter?.itemCount ?: 0
                    if (itemCount > 1) {
                        val nextItem = (viewPager.currentItem + 1) % itemCount
                        viewPager.setCurrentItem(nextItem, true)
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
        _binding = null // 清除绑定避免内存泄漏
    }

    // 停止轮播
    private fun stopPlay() {
        isPlaying = false
        handler.removeCallbacksAndMessages(null)
    }
}