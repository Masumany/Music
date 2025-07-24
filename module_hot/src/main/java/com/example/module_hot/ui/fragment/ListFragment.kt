package com.example.module_hot.ui.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.module_hot.R
import com.example.module_hot.adapter.ListAdapter
import com.example.module_hot.databinding.FragmentListBinding
import com.example.module_hot.viewModel.ListViewModel
import com.example.module_hot.viewModel.LoadState

class ListFragment : Fragment() {

    companion object {
        fun newInstance() = ListFragment()
    }

    private val viewModel: ListViewModel by  viewModels()
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        ListAdapter (
            onListItemClick = {
                Toast.makeText(requireContext(), it.listName, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvList.adapter = adapter
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())

        loadListData()
    }
    private fun loadListData() {
        viewModel.loadListData()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listSongsData.collect { list ->
                adapter.submitList(list)
                binding.rvList.scrollToPosition(0)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbList.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbList.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbList.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbList.visibility = android.view.View.GONE
                        Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbList.visibility = android.view.View.GONE
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