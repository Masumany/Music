package com.example.moudle_search.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.adapter.SongListsAdapter
import com.example.moudle_search.databinding.FragmentSongListsBinding
import com.example.moudle_search.ui.activity.ListSongsActivity
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SongListViewModel
import kotlinx.coroutines.launch

class SongListsFragment : Fragment(), SearchResultAdapter.Searchable {

    // 伴生对象
    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = SongListsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    // 成员变量
    private val viewModel: SongListViewModel by viewModels() // ViewModel 优先
    private var _binding: FragmentSongListsBinding? = null
    private val binding get() = _binding!!
    private var fragmentContext: android.content.Context? = null
    private var currentKeyword: String = "" // 当前搜索关键词

    //适配器
    private val adapter by lazy {
        SongListsAdapter(
            onItemClick = { songList ->
                fragmentContext?.let { context ->
                    Toast.makeText(context, songList.name, Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, ListSongsActivity::class.java)
                    intent.putExtra("id", songList.id)
                    intent.putExtra("name", songList.name)
                    Log.d("SongListsFragment", "id: ${songList.id}")
                    if (isAdded) {
                        startActivity(intent)
                    }
                }
            }
        )
    }

    // 生命周期方法
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentKeyword = arguments?.getString(ARG_KEYWORDS).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentContext = requireContext().applicationContext
        binding.rvSongLists.adapter = adapter
        binding.rvSongLists.layoutManager = LinearLayoutManager(context)
        loadListsData(currentKeyword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSongLists.adapter = null
        _binding = null
        fragmentContext = null
    }

    // 实现的接口方法
    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getListsData(keyword)
        }
    }

    // 核心业务方法
    private fun loadListsData(keywords: String) {
        viewModel.getListsData(keywords)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.listsResult.collect { lists ->
//                        // 安全处理空值情况
//                        adapter.submitList(lists.result?.playlists ?: emptyList())
//
//                        // 可以添加空数据状态显示逻辑
//                        if (lists.result?.playlists.isNullOrEmpty()) {
//                            binding.emptyView.visibility = View.VISIBLE
//                        } else {
//                            binding.emptyView.visibility = View.GONE
//                        }
//                    }
//                }
                        adapter.submitList(lists.result.playlists)
                    }
                }
                launch {
                    viewModel.loadState.collect {
                        binding.pbLists.visibility = when (it) {
                            is LoadState.Init, is LoadState.Loading -> View.VISIBLE
                            else -> View.GONE
                        }
                        if (it is LoadState.Error) {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}