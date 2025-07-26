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
import com.example.moudle_search.adapter.SingersAdapter
import com.example.moudle_search.databinding.FragmentSingersBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SingerViewModel
import com.therouter.TheRouter

class SingersFragment : Fragment(), SearchResultAdapter.Searchable {

    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getSingerData(keyword)
        }
    }

    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = SingersFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    private val viewModel: SingerViewModel by viewModels()
    private lateinit var _binding: FragmentSingersBinding
    private val binding get() = _binding!!

    private val adapter by lazy {
        SingersAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                TheRouter.build("/singer/SingerActivity")
                    .withLong("id", it.id.toLong())
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
        _binding = FragmentSingersBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSingers.adapter = adapter
        binding.rvSingers.layoutManager = LinearLayoutManager(requireContext())

        loadSingersData (currentKeyword)
    }
    private fun loadSingersData (keywords: String) {
        viewModel.getSingerData(keywords)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.singerResult.collect {
                val artistList = it.result.singers
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
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}