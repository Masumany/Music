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
import com.therouter.TheRouter

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
                TheRouter.build("/singer/SingerActivity")
                    .withLong("id", it.id)
                    .navigation(requireActivity())
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
        // 初始化 SwipeRefreshLayout
        binding.singerSwipeRefresh.apply {
            // 设置刷新动画的颜色
            setColorSchemeResources(R.color.black, R.color.white)

            // 设置下拉刷新的监听器
            setOnRefreshListener {
                // 下拉时触发数据刷新
                loadSingerData()
            }
        }

        loadSingerData()
    }

    private fun loadSingerData() {
        viewModel.getSingerData()
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
                        if (binding.singerSwipeRefresh.isRefreshing) {
                            binding.pbSinger.visibility = View.GONE
                        } else {
                            binding.pbSinger.visibility = View.VISIBLE
                        }
                    }
                    is LoadState.Success -> {
                        binding.pbSinger.visibility = android.view.View.GONE
                        binding.singerSwipeRefresh.isRefreshing = false
                    }
                    is LoadState.Error -> {
                        binding.pbSinger.visibility = android.view.View.GONE
                        binding.singerSwipeRefresh.isRefreshing = false
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