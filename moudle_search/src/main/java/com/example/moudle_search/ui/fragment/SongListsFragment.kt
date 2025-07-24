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
import com.example.moudle_search.adapter.SongListsAdapter
import com.example.moudle_search.databinding.FragmentSongListsBinding
import com.example.moudle_search.databinding.SongListsItemBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SharedViewModel

class SongListsFragment : Fragment() {

    companion object {
        fun newInstance() = SongListsFragment()
    }

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var _binding: FragmentSongListsBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        SongListsAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                //  跳转
            }
        )
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
        binding.rvSongLists.adapter = adapter
        binding.rvSongLists.layoutManager = LinearLayoutManager(requireContext())

        loadSingersData ()
    }
    private fun loadSingersData () {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchResult.collect { searchData ->
                // 从 SearchData 中提取歌手列表
                val songList = searchData.result?.playlist ?: emptyList()
                adapter.submitList(songList)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbLists.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbLists.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbLists.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbLists.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbLists.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}