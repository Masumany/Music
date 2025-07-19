package com.example.module_personage.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_login_register.databinding.FragmentHistoryBinding
import com.example.module_personage.adapter.HistoryAdapter
import com.example.module_personage.viewModel.HistoryViewModel
import com.example.module_personage.viewModel.LoadState

class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()

    private val adapter by lazy {
        HistoryAdapter(
            onItemClick = {
                Toast.makeText(requireContext(), "播放《${it.name}》", Toast.LENGTH_SHORT).show()
            },
            onPlayClick = {
                Toast.makeText(requireContext(), "播放《${it.name}》", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.adapter = adapter
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())

        loadHistoryData()
    }
    private fun loadHistoryData() {
        val uid = arguments?.getString("uid") ?: ""
        viewModel.loadHistory(uid)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyData.collect { historyData ->
                val songList = historyData.weekData.map { it.song }
                adapter.submitList(songList)
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbHistory.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbHistory.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbHistory.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbHistory.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbHistory.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}