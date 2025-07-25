package com.example.moudle_search.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.R
import com.example.moudle_search.adapter.SingersAdapter
import com.example.moudle_search.databinding.FragmentSingersBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SharedViewModel
import kotlinx.coroutines.launch

class SingersFragment : Fragment() {

    companion object {
        fun newInstance() = SingersFragment()
    }

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var _binding: FragmentSingersBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        SingersAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                //  跳转到歌手详情页面
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingersBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSingers.adapter = adapter
        binding.rvSingers.layoutManager = LinearLayoutManager(requireContext())

        loadSingersData ()
    }
    private fun loadSingersData () {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchResult.collect { searchData ->
                // 从 SearchData 中提取歌手列表
                val artistList = searchData.result?.artist ?: emptyList()
                adapter.submitList(artistList)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbSingers.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbSingers.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbSingers.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbSingers.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbSingers.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}