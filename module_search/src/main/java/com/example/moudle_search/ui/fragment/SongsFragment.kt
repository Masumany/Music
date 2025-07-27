package com.example.moudle_search.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.adapter.SongsAdapter
import com.example.moudle_search.databinding.FragmentSongsBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SongsViewModel
import com.therouter.TheRouter
import Adapter.MusicDataCache  // 导入缓存适配器
import android.util.Log
import data.ListMusicData
import data.SongsResultData

class SongsFragment : Fragment(), SearchResultAdapter.Searchable {

    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getSongsData(keyword)
        }
    }

    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = SongsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    private val viewModel: SongsViewModel by viewModels()
    private lateinit var _binding: FragmentSongsBinding
    private val binding get() = _binding!!

    // 保存当前搜索到的完整歌曲列表（用于缓存）
    private var currentSongList: List<ListMusicData.Song> = emptyList()

    private val adapter by lazy {
        SongsAdapter(
            onItemClick = { song ->
                // 点击歌曲时，将当前完整列表存入缓存
                MusicDataCache.currentSongList = currentSongList
                // 获取当前点击歌曲在列表中的位置
                val currentPosition = currentSongList.indexOfFirst { it.id == song.id }

                Toast.makeText(requireContext(), song.name, Toast.LENGTH_SHORT).show()
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withString("songListName", song.name)
                    .withString("cover", song.al.picUrl)
                    .withLong("id", song.id)
                    .withString("athour", song.ar[0].name ?: "未知")
                    .withLong("singerId", song.ar[0].id)  // 使用正确的getId()方法
                    .withInt("currentPosition", currentPosition)  // 传递当前歌曲位置
                    .navigation(this)
            }
        )
    }

    private var currentKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentKeyword = arguments?.getString(ARG_KEYWORDS).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())

        loadSongsData(currentKeyword)
    }

    private fun loadSongsData(keywords: String) {
        viewModel.getSongsData(keywords)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songsResult.collect { resultData ->
                // 转换数据并保存到当前列表变量
                currentSongList = resultData?.let { DataConverter.convertSongsResultData(it) } ?: emptyList()
                // 提交数据给适配器
                adapter.submitList(currentSongList)

                // 调试信息：显示当前列表大小
                if (currentSongList.isNotEmpty()) {
                    Log.d("SongsFragment", "加载歌曲成功，共 ${currentSongList.size} 首")
                } else {
                    Log.d("SongsFragment", "未加载到歌曲数据")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect { state ->
                when (state) {
                    is LoadState.Init -> binding.pbSongs.visibility = View.VISIBLE
                    is LoadState.Loading -> binding.pbSongs.visibility = View.VISIBLE
                    is LoadState.Success -> binding.pbSongs.visibility = View.GONE
                    is LoadState.Error -> {
                        binding.pbSongs.visibility = View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> binding.pbSongs.visibility = View.GONE
                }
            }
        }
    }
}
