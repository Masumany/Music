package com.example.moudle_search.ui.fragment

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.adapter.SongListsAdapter
import com.example.moudle_search.databinding.FragmentSongListsBinding
import com.example.moudle_search.ui.activity.ListSongsActivity
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SongListViewModel

class SongListsFragment : Fragment(), SearchResultAdapter.Searchable  {

    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getListsData(keyword)
        }
    }

    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = SongListsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    private val viewModel: SongListViewModel by viewModels()
    private  var _binding: FragmentSongListsBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        SongListsAdapter (
            onItemClick = {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), ListSongsActivity::class.java)
                intent.putExtra("id", it.id)
                intent.putExtra("name", it.name)
                Log.d("SongListsFragment", "id: ${it.id}")
                startActivity(intent)
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
        _binding = FragmentSongListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSongLists.adapter = null
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSongLists.adapter = adapter
        binding.rvSongLists.layoutManager = LinearLayoutManager(requireContext())

        loadListsData (currentKeyword)
    }
    private fun loadListsData (keywords: String) {
        viewModel.getListsData(keywords)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listsResult.collect {
                val songList = it.result.playlists
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