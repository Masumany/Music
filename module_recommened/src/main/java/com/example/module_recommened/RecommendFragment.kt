package com.example.module_recommened

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import kotlinx.coroutines.launch

class RecommendFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var rv_list: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // 其他变量保持不变...
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

    // 滑动冲突处理相关变量
    private var lastX = 0f
    private var lastY = 0f
    private val SCROLL_THRESHOLD = 5  // 滑动方向判断阈值
    private var isHorizontalScrolling = false  // 是否正在横向滚动
    private var isBannerTouching = false  // 是否正在触摸Banner区域

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
        rv_list = binding.rvlist
        viewPager = binding.viewPager
        recyclerView = binding.recyclerView

        // 初始化代码保持不变...
        rv_list.adapter = songAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        rv_list.layoutManager = layoutManager

        // 纵向RecyclerView滚动监听
        rv_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isHorizontalScrolling) return

                val lastPosition = layoutManager.findLastVisibleItemPosition()
                val totalItem = layoutManager.itemCount

                if (lastPosition >= totalItem - 1 && hasMoreData && !isLoading) {
                    loadNextPage()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    recyclerView.requestDisallowInterceptTouchEvent(true)
                    viewPager.requestDisallowInterceptTouchEvent(true)
                }
            }
        })

        // 横向RecyclerView设置
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerView.setOnTouchListener { _, event ->
            handleHorizontalTouchEvent(event, recyclerView, false)
            false
        }

        // ViewPager2(Banner)触摸事件处理 - 重点修改
        viewPager.setOnTouchListener { _, event ->
            // 标记是否正在触摸Banner区域
            isBannerTouching = when(event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> false
                else -> isBannerTouching
            }

            // 处理Banner滑动时的下拉刷新控制
            controlRefreshWhenBannerTouching()

            // 处理横向滑动事件
            handleHorizontalTouchEvent(event, viewPager, true)
            false
        }

        // 下拉刷新监听器 - 增加判断条件
        swipeRefreshLayout.setOnRefreshListener {
            // 只有不在触摸Banner时才允许刷新
            if (!isBannerTouching) {
                lifecycleScope.launch {
                    refreshData()
                    swipeRefreshLayout.isRefreshing = false
                }
            } else {
                // 如果正在触摸Banner，立即取消刷新
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 初始化ViewModel和加载数据
        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        recommendedViewModel = ViewModelProvider(this)[RecommenedViewModel::class.java]
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
        loadData()
    }

    // 控制Banner触摸时的下拉刷新状态
    private fun controlRefreshWhenBannerTouching() {
        // 如果正在触摸Banner区域，禁止下拉刷新
        swipeRefreshLayout.isEnabled = !isBannerTouching
    }

    // 处理横向滑动组件的触摸事件
    private fun handleHorizontalTouchEvent(event: MotionEvent, view: View, isBanner: Boolean): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                rv_list.requestDisallowInterceptTouchEvent(true)
                isHorizontalScrolling = false

                // 如果是Banner，标记触摸状态
                if (isBanner) {
                    isBannerTouching = true
                    controlRefreshWhenBannerTouching()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x
                val currentY = event.y
                val dx = currentX - lastX
                val dy = currentY - lastY

                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SCROLL_THRESHOLD) {
                    isHorizontalScrolling = true
                    rv_list.requestDisallowInterceptTouchEvent(true)
                } else if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    isHorizontalScrolling = false
                    val canScroll = when (view) {
                        is ViewPager2 -> view.canScrollHorizontally(-dx.toInt())
                        is RecyclerView -> view.canScrollHorizontally(-dx.toInt())
                        else -> false
                    }
                    if (!canScroll) {
                        rv_list.requestDisallowInterceptTouchEvent(false)
                    }
                }
                lastX = currentX
                lastY = currentY
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isHorizontalScrolling = false
                rv_list.requestDisallowInterceptTouchEvent(false)

                // 触摸结束，恢复Banner触摸状态
                if (isBanner) {
                    isBannerTouching = false
                    controlRefreshWhenBannerTouching()
                }
            }
        }
        return false
    }

    // 以下方法保持不变...
    private fun refreshData() {
        currentPage = 1
        lifecycleScope.launch {
            loadFirstPage()
        }
    }

    private fun loadFirstPage() {
        currentPage = 1
        fetchListData(currentPage)
    }

    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true
        currentPage++
        fetchListData(currentPage)
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                launch { fetchBannerData() }
                launch { fetchRecommendedData() }
                launch { loadFirstPage() }
            } catch (e: Exception) {
                Log.e("RecommendFragment", "加载数据失败: ${e.message}")
            }
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
            Log.e("BannerData", "轮播图加载失败: ${e.message}")
        }
    }

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
                isLoading = false
            }
        }
    }

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
        _binding = null
    }

    private fun stopPlay() {
        isPlaying = false
        handler.removeCallbacksAndMessages(null)
    }
}
