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
import com.example.moudle_search.ui.fragment.SingersFragment.Companion
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SongsViewModel
import com.therouter.TheRouter

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

    private val adapter by lazy {
        SongsAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withString("songListName", it.name)
                    .withString("cover", it.al.picUrl)
                    .withLong("id", it.id)
                    .withString("athour", it.ar[0].name?: "未知")
                    .withLong("singerId", it.ar[0].id.toLong()?: 0)
                    .navigation(this)
            }
        )
    }
    // 当前搜索关键词
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

        loadSingersData (currentKeyword)
    }
    private fun loadSingersData (keywords: String) {
        viewModel.getSongsData(keywords)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songsResult.collect {
                val songList = it.result.songs
                adapter.submitList(songList)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbSongs.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbSongs.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbSongs.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbSongs.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbSongs.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}