package com.example.module_hot.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_hot.R
import com.example.module_hot.adapter.MvAdapter
import com.example.module_hot.databinding.FragmentMvRankBinding
import com.example.module_hot.viewModel.LoadState
import com.example.module_hot.viewModel.MvRankViewModel

class MvRankFragment : Fragment() {

    companion object {
        fun newInstance() = MvRankFragment()
    }

    private val viewModel: MvRankViewModel by viewModels()
    private lateinit var _binding : FragmentMvRankBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        MvAdapter(
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                //  跳转到详情页面
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMvRankBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMvRank.adapter = adapter
        binding.rvMvRank.layoutManager = LinearLayoutManager(requireContext())

        loadMvData()
    }

    private fun loadMvData() {
        viewModel.getMvData()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.mvData.collect { list ->
                adapter.submitList(list.data)
                binding.rvMvRank.scrollToPosition(0)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbMvRank.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbMvRank.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbMvRank.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbMvRank.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbMvRank.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}