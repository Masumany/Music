package com.example.module_hot.ui.fragment


import android.content.Intent
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
import com.example.module_hot.ui.activity.ListSongsActivity
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
                val intent = Intent(requireActivity(), ListSongsActivity::class.java)
                intent.putExtra("id", it.id)
                intent.putExtra("name", it.listName)
                startActivity(intent)
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
        // 初始化 SwipeRefreshLayout
        binding.listSwipeRefresh.apply {
            // 设置刷新动画的颜色
            setColorSchemeResources(R.color.black, R.color.white)

            // 设置下拉刷新的监听器
            setOnRefreshListener {
                // 下拉时触发数据刷新
                loadListData()
            }
        }

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