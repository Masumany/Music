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
import com.example.lib.base.Song
import com.example.music.databinding.FragmentMusicplayerlistBinding
import com.example.yourproject.converter.DataConverter

class MusicPlayerListFragment : Fragment() {

    private lateinit var binding: FragmentMusicplayerlistBinding
    private val songAdapter = LiAdapter()
    private lateinit var listViewModel: ListViewModel

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
        Log.d("ListFragment", "songAdapter 是否为空: ${songAdapter == null}")
        Log.d("ListFragment", "rvlist 是否为空: ${binding.rvlist == null}")
        initViewModel()
        initRecyclerView()
        loadFirstPage()  // 加载第一页数据
    }

    private fun initViewModel() {
        listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
    }

    private fun initRecyclerView() {
        binding.rvlist.adapter = songAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvlist.layoutManager = layoutManager

        // 滚动监听（实现分页加载）
        binding.rvlist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 计算最后一项的位置
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 当滚动到列表底部且有更多数据时，加载下一页
                if (lastVisiblePosition >= totalItemCount - 1 && hasMoreData && !isLoading) {
                    loadNextPage()
                }
            }
        })
    }

    // 加载第一页数据（重置）
    private fun loadFirstPage() {
        currentPage = 1
        fetchListData(currentPage)
    }

    // 加载下一页数据
    private fun loadNextPage() {
        if (isLoading) return
        isLoading = true
        currentPage++
        fetchListData(currentPage)
    }

    private fun fetchListData(page: Int) {
        lifecycleScope.launch {
            try {
                val result = listViewModel.getListData(page, pageSize)
                Log.d("Mp", "数据响应: code=${result.code}")

                if (result.code == 200) {
                    val originalSongs: List<Song> = result.data?.dailySongs ?: emptyList()
                    val convertedSongs = DataConverter.convertBaseSongList(originalSongs)
                    hasMoreData = convertedSongs?.size ?: 0 >= pageSize  // 判断是否还有更多数据

                    // 第一页替换数据，后续页追加数据
                    if (page == 1) {
                        songAdapter.submitList(convertedSongs)
                    } else {
                        if (convertedSongs != null) {
                            songAdapter.addMoreData(convertedSongs)
                        }
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