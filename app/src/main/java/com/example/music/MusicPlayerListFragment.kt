package com.example.module_musicplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.module_musicplayer.databinding.FragmentListBinding
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.viewmodel.ListViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.music.databinding.FragmentMusicplayerlistBinding

class MusicPlayerListFragment : Fragment() {

    private lateinit var binding: FragmentMusicplayerlistBinding
    private val songAdapter = LiAdapter()  // 复用RecommendFragment的适配器
    private lateinit var listViewModel: ListViewModel  // 复用RecommendFragment的ViewModel

    // 分页参数（与RecommendFragment保持一致）
    private var currentPage = 1
    private val pageSize = 5
    private var hasMoreData = true
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicplayerlistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ListFragment", "songAdapter 是否为空: ${songAdapter == null}")  // 应输出 false
        Log.d("ListFragment", "rvlist 是否为空: ${binding.rvlist == null}")
        initViewModel()
        initRecyclerView()
        loadFirstPage()  // 加载第一页数据
    }

    // 初始化ViewModel（与RecommendFragment使用同一个）
    private fun initViewModel() {
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
    }

    // 初始化RecyclerView（完全复制RecommendFragment的配置）
    private fun initRecyclerView() {
        // 设置布局管理器（与RecommendFragment一致）
        binding.rvlist.adapter = songAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvlist.layoutManager = layoutManager

        // 滚动监听（实现分页加载，与RecommendFragment逻辑相同）
        binding.rvlist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 计算最后可见项位置
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 当滚动到列表底部且有更多数据时，加载下一页
                if (lastVisiblePosition >= totalItemCount - 1 && hasMoreData && !isLoading) {
                    loadNextPage()
                }
            }
        })
    }

    // 加载第一页数据（重置分页参数）
    private fun loadFirstPage() {
        currentPage = 1
        fetchListData(currentPage)
    }

    // 加载下一页数据
    private fun loadNextPage() {
        if (isLoading) return  // 防止重复加载
        isLoading = true
        currentPage++
        fetchListData(currentPage)
    }

    // 请求列表数据（与RecommendFragment完全相同的逻辑）
    // 请求列表数据（与RecommendFragment完全相同的逻辑）
    private fun fetchListData(page: Int) {
        lifecycleScope.launch {
            try {
                // 调用与RecommendFragment相同的ViewModel方法获取数据
                val result = listViewModel.getListData(page, pageSize)
                Log.d("Mp", "数据响应: code=${result.code}")

                if (result.code == 200) {
                    val songs = result.data?.dailySongs  // 与RecommendFragment的数据字段一致
                    hasMoreData = songs?.size ?: 0 >= pageSize  // 判断是否还有更多数据

                    // 第一页替换数据，后续页追加数据
                    if (page == 1) {
                        songAdapter.submitList(songs)  // 假设适配器有submitList方法
                    } else {
                        if (songs != null) {
                            songAdapter.addMoreData(songs)
                        }  // 假设适配器有addMoreData方法
                    }
                    songAdapter.notifyDataSetChanged()  // 刷新列表
                } else {
                    hasMoreData = false
                    Log.e("MP", "数据错误: code=${result.code}")
                }
            } catch (e: Exception) {
                Log.e("MP", "加载失败: ${e.message}")
                Toast.makeText(context, "列表加载失败", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false  // 无论成功失败，都标记为未加载
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 清除适配器引用，避免内存泄漏
        binding.rvlist.adapter = null
    }
}