package com.example.module_recommened.ui

import android.graphics.Rect
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
import com.example.module_recommened.adapter.BannerAdapter
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.adapter.ReAdapter
import com.example.module_recommened.databinding.FragmentRecommendBinding
import com.example.module_recommened.viewmodel.BannerViewModel
import com.example.module_recommened.viewmodel.ListViewModel
import com.example.module_recommened.viewmodel.RecommenedViewModel
import com.example.yourproject.converter.DataConverter
import kotlinx.coroutines.launch
import com.example.lib.base.Song as BaseSong

class RecommendFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var rv_list: RecyclerView
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

    // 滑动冲突处理相关变量
    private var lastX = 0f
    private var lastY = 0f
    private val SCROLL_THRESHOLD = 5  // 滑动方向判断阈值（px）
    private var isHorizontalScrolling = false  // 是否正在横向滚动
    private var isInHorizontalArea = false  // 是否在横向组件区域内

    private var bannerRect = Rect()
    private var horizontalRvRect = Rect()

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

        // 初始化列表
        rv_list.adapter = songAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        rv_list.layoutManager = layoutManager

        // 获取横向组件的区域范围
        view.post {
            viewPager.getGlobalVisibleRect(bannerRect)
            recyclerView.getGlobalVisibleRect(horizontalRvRect)
        }

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
                    // 纵向滑动时允许刷新，但要判断是否在横向区域
                    recyclerView.requestDisallowInterceptTouchEvent(!isInHorizontalArea)
                    viewPager.requestDisallowInterceptTouchEvent(isInHorizontalArea)
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
            handleHorizontalTouchEvent(event, recyclerView)
            false
        }

        // Banner触摸事件处理
        viewPager.setOnTouchListener { _, event ->
            handleHorizontalTouchEvent(event, viewPager)
            false
        }

        // 根布局触摸事件，判断是否在横向组件区域内
        binding.root.setOnTouchListener { _, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()

            // 判断触摸点是否在Banner或横向RecyclerView内
            isInHorizontalArea = bannerRect.contains(x, y) || horizontalRvRect.contains(x, y)

            // 在横向区域内，禁用下拉刷新
            if (isInHorizontalArea) {
                swipeRefreshLayout.isEnabled = false
            } else if (event.action == MotionEvent.ACTION_UP) {
                // 离开横向区域，恢复下拉刷新
                swipeRefreshLayout.isEnabled = true
                isHorizontalScrolling = false
            }
            false
        }

        // 下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener {
            // 只有不在横向区域且非横向滑动时才执行刷新
            if (!isHorizontalScrolling && !isInHorizontalArea) {
                lifecycleScope.launch {
                    refreshData()
                    swipeRefreshLayout.isRefreshing = false
                }
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 初始化ViewModel和加载数据
        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        recommendedViewModel = ViewModelProvider(this)[RecommenedViewModel::class.java]
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
        loadData()

        // 初始状态允许刷新
        swipeRefreshLayout.isEnabled = true
    }

    // 处理横向滑动组件的触摸事件
    private fun handleHorizontalTouchEvent(event: MotionEvent, view: View): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                rv_list.requestDisallowInterceptTouchEvent(true)
                isHorizontalScrolling = false
                swipeRefreshLayout.isEnabled = false  // 触摸横向组件时禁用刷新
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x
                val currentY = event.y
                val dx = currentX - lastX  // 横向位移
                val dy = currentY - lastY  // 纵向位移

                // 只要有横向滑动就判定为横向滚动
                if (Math.abs(dx) > SCROLL_THRESHOLD) {
                    isHorizontalScrolling = true
                    swipeRefreshLayout.isEnabled = false  // 禁用刷新
                    rv_list.requestDisallowInterceptTouchEvent(true)
                } else if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    // 纵向滑动时，只有当横向组件无法横向滚动时才允许纵向滚动
                    val canScroll = when (view) {
                        is ViewPager2 -> view.canScrollHorizontally(-dx.toInt())
                        is RecyclerView -> view.canScrollHorizontally(-dx.toInt())
                        else -> false
                    }
                    if (!canScroll) {
                        isHorizontalScrolling = false
                        rv_list.requestDisallowInterceptTouchEvent(false)
                    } else {
                        swipeRefreshLayout.isEnabled = false
                    }
                }

                lastX = currentX
                lastY = currentY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 触摸结束，延迟恢复刷新状态，防止误触发
                isHorizontalScrolling = false
                handler.postDelayed({
                    if (!isInHorizontalArea) {
                        swipeRefreshLayout.isEnabled = true
                    }
                }, 100)
                rv_list.requestDisallowInterceptTouchEvent(false)
            }
        }
        return false
    }

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
