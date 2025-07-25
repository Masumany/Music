package com.example.moudle_search.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.R
import com.example.moudle_search.adapter.SingersAdapter
import com.example.moudle_search.adapter.VideoAdapter
import com.example.moudle_search.databinding.FragmentSingersBinding
import com.example.moudle_search.databinding.FragmentVideosBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SharedViewModel

class VideosFragment : Fragment() {

    companion object {
        fun newInstance() = VideosFragment()
    }

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var _binding: FragmentVideosBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        VideoAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.title, Toast.LENGTH_SHORT).show()
                //
            }
        )
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

        loadVideosData ()
    }
    private fun loadVideosData () {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchResult.collect { searchData ->
                // 从 SearchData 中提取歌手列表
                val videoList = searchData.result?.new_mlog ?: emptyList()
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