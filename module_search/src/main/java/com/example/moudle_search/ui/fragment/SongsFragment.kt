package com.example.moudle_search.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.adapter.SongsAdapter
import com.example.moudle_search.databinding.FragmentSongsBinding
import com.example.moudle_search.ui.fragment.SingersFragment.Companion
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SongsViewModel
import com.therouter.TheRouter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class SongsFragment : Fragment(), SearchResultAdapter.Searchable {

    override fun onNewSearch(keyword: String) {
        if (keyword != currentKeyword) {
            currentKeyword = keyword
            viewModel.getSongsData(keyword)
        }
    }
    companion object {
        private const val ARG_KEYWORDS = "keywords"
        fun newInstance(keywords: String) = SongsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORDS, keywords)
            }
        }
    }

    private val viewModel: SongsViewModel by viewModels()
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    // 使用弱引用存储上下文，避免强引用持有
    private var weakContext = WeakReference<Fragment>(this)

    private val adapter by lazy {
        SongsAdapter (
            onItemClick = { song ->
                // 通过弱引用获取上下文，避免直接持有Fragment
                weakContext.get()?.let { fragment ->
                    fragment.context?.let { context ->
                        try {
                            // 用应用上下文显示Toast，避免持有Activity
                            Toast.makeText(context.applicationContext, song.name, Toast.LENGTH_SHORT).show()
                            val author = if (song.ar.isNotEmpty()) song.ar[0].name ?: "未知歌手" else "未知歌手"
                            TheRouter.build("/module_musicplayer/musicplayer")
                                .withString("songListName", song.name ?: "未知歌曲")
                                .withString("cover", song.al.picUrl ?: "")
                                .withLong("id", song.id)
                                .withString("athour", author)
                                // 检查Fragment是否已附加到Activity
                                .navigation(if (fragment.isAdded) fragment.requireActivity() else null)
                        } catch (e: Exception) {
                            Log.e("SongsFragment", "跳转音乐播放器失败: ${e.message}")
                        }
                    }
                }
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
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSongs.adapter = null
        adapter.onItemClick = null
        _binding = null
        // 清除适配器回调引用
        adapter.onItemClick = null
        // 清除弱引用，加速回收
        weakContext.clear()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(context)

        loadSongsData (currentKeyword)
    }
    private fun loadSongsData (keywords: String) {
        viewModel.getSongsData(keywords)
        // 使用repeatOnLifecycle确保协程在生命周期外自动取消
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.songsResult.collect { songs ->
                        adapter.submitList(songs.result.songs)
                    }
                }
                launch {
                    viewModel.loadState.collect {
                        when (it) {
                            is LoadState.Init -> {
                                binding.pbSongs.visibility = android.view.View.VISIBLE
                            }

                            is LoadState.Loading -> {
                                binding.pbSongs.visibility = android.view.View.VISIBLE
                            }

                            is LoadState.Success -> {
                                binding.pbSongs.visibility = android.view.View.GONE
                            }

                            is LoadState.Error -> {
                                binding.pbSongs.visibility = android.view.View.GONE
                                Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            else -> {
                                binding.pbSongs.visibility = android.view.View.GONE
                            }
                        }
                    }
                }
            }
        }
    }
}