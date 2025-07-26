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
import com.example.moudle_search.adapter.VideoAdapter
import com.example.moudle_search.databinding.FragmentVideosBinding
import com.example.moudle_search.ui.fragment.SongsFragment.Companion
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.VideosViewModel
import com.therouter.TheRouter

class VideosFragment : Fragment(), SearchResultAdapter.Searchable {

    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getVideosData(keyword)
        }
    }

    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = VideosFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    private val viewModel: VideosViewModel by viewModels()
    private lateinit var _binding: FragmentVideosBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        VideoAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                TheRouter.build("/module_mvplayer/mvplayer")
                    .withString("mvId", it.id.toString())
                    .navigation(requireActivity())
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
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvVideos.adapter = adapter
        binding.rvVideos.layoutManager = LinearLayoutManager(requireContext())

        loadVideosData (currentKeyword)
    }
    private fun loadVideosData (keywords: String) {
        viewModel.getVideosData(keywords)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.videosResult.collect {
                val videoList = it.result.mvs
                adapter.submitList(videoList)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbVideos.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbVideos.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbVideos.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbVideos.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbVideos.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}