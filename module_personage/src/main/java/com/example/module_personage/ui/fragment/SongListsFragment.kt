package com.example.module_personage.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_login_register.databinding.FragmentSongListsBinding
import com.example.module_personage.adapter.SongListsAdapter
import com.example.module_personage.ui.activity.ListSongsActivity
import com.example.module_personage.viewModel.LoadState
import com.example.module_personage.viewModel.SongListViewModel
import kotlinx.coroutines.launch

class SongListsFragment : Fragment(){

    companion object {
        fun newInstance(): SongListsFragment {
            return SongListsFragment()
        }
    }
    // 成员变量
    private val viewModel: SongListViewModel by viewModels() // ViewModel 优先
    private var _binding: FragmentSongListsBinding? = null
    private val binding get() = _binding!!
    private var fragmentContext: android.content.Context? = null

    //适配器
    private val adapter by lazy {
        SongListsAdapter(
            onItemClick = { songList ->
                fragmentContext?.let { context ->
                    Toast.makeText(context, songList.name, Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, ListSongsActivity::class.java)
                    intent.putExtra("id", songList.id)
                    intent.putExtra("name", songList.name)
                    Log.d("SongListsFragment", "id: ${songList.id}")
                    if (isAdded) {
                        startActivity(intent)
                    }
                }
            }
        )
    }

    // 生命周期方法
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        fragmentContext = requireContext().applicationContext
        binding.rvSongLists.adapter = adapter
        binding.rvSongLists.layoutManager = LinearLayoutManager(context)
        loadListsData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSongLists.adapter = null
        _binding = null
        fragmentContext = null
    }

    // 核心业务方法
    private fun loadListsData() {
        viewModel.getListsData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    viewModel.listsResult.collect { lists ->
                        adapter.submitList(lists.result?.playlists ?: emptyList())
                    }
                }
                launch {
                    viewModel.loadState.collect {
                        binding.pbLists.visibility = when (it) {
                            is LoadState.Init, is LoadState.Loading -> View.VISIBLE
                            else -> View.GONE
                        }
                        if (it is LoadState.Error) {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}