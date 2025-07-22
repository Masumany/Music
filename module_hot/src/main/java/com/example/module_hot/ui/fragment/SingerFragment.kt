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
import com.example.module_hot.adapter.SingerAdapter
import com.example.module_hot.databinding.FragmentListBinding
import com.example.module_hot.databinding.FragmentSingerBinding
import com.example.module_hot.viewModel.LoadState
import com.example.module_hot.viewModel.SingerViewModel

class SingerFragment : Fragment() {

    companion object {
        fun newInstance() = SingerFragment()
    }

    private val viewModel: SingerViewModel by viewModels()
    private lateinit var _binding: FragmentSingerBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        SingerAdapter (
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
        _binding = FragmentSingerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSinger.adapter = adapter
        binding.rvSinger.layoutManager = LinearLayoutManager(requireContext())

        loadSingerData()
    }

    private fun loadSingerData() {
        viewModel.singerData
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.singerData.collect { list ->
                adapter.submitList(list.list.artists)
                binding.rvSinger.scrollToPosition(0)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbSinger.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbSinger.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbSinger.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbSinger.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbSinger.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}