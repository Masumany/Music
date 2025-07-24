package com.example.module_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.module_details.databinding.FragmentSingersongBinding
import com.therouter.TheRouter
import kotlinx.coroutines.launch
import Adapter.TopAdapter
import TopViewModel
import androidx.recyclerview.widget.RecyclerView

class SingerSongFragment : Fragment() {
    private lateinit var binding: FragmentSingersongBinding
    private lateinit var topList: RecyclerView
    private lateinit var topViewModel: TopViewModel
    private var singerId: Long? = null
    private lateinit var topAdapter: TopAdapter  // 使用成员变量适配器


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingersongBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 接收歌手ID的方法
    fun setSingerId(id: Long) {
        if (id <= 0) {
            Log.e("SingerSong", "setSingerId: 无效ID=$id（必须>0）")
            return
        }
        this.singerId = id
        Log.d("SingerSong", "setSingerId: 成功接收ID=$id")

        // 如果Fragment已初始化完成，立即请求数据
        if (isAdded && ::topViewModel.isInitialized) {
            Log.d("SingerSong", "ID已接收且Fragment就绪，立即请求数据")
            fetchTopData()
        } else {
            Log.d("SingerSong", "ID已接收，等待Fragment初始化完成后请求")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 初始化RecyclerView
        topList = binding.topRv
        topList.layoutManager = LinearLayoutManager(requireContext())

        // 2. 初始化适配器并设置数据变化监听
        topAdapter = TopAdapter()
        topList.adapter = topAdapter

        // 添加数据变化监听器，实时更新歌曲数量
        topAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                updateSongCount()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateSongCount()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                updateSongCount()
            }
        })

        Log.d("SingerSong", "RecyclerView初始化完成")

        // 3. 初始化ViewModel
        topViewModel = ViewModelProvider(this)[TopViewModel::class.java]
        Log.d("SingerSong", "ViewModel初始化完成")

        // 4. 观察数据变化
        topViewModel.topData.observe(viewLifecycleOwner) { result ->
            Log.d("SingerSong", "观察到数据变化：${result?.songs?.size ?: 0}首歌")
            if (result != null && result.songs.isNotEmpty()) {
                topAdapter.submitList(result.songs)
                Log.d("SingerSong", "数据更新成功，共${result.songs.size}首歌")
            } else {
                Log.d("SingerSong", "数据为空或歌曲列表为空")
                topAdapter.submitList(emptyList()) // 确保空列表时也更新UI
            }
        }

        // 5. 若已接收ID，立即请求数据
        if (singerId != null) {
            Log.d("SingerSong", "onViewCreated中已获取ID=$singerId，开始请求数据")
            fetchTopData()
        } else {
            Log.d("SingerSong", "onViewCreated中未获取到ID，等待setSingerId调用")
        }

        // 6. 全部播放按钮点击事件
        binding.topAllstart.setOnClickListener{
            val firstSong = topAdapter.currentList.firstOrNull()
            if (firstSong != null) {
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withString("id", firstSong.id.toString())
                    .withString("cover", firstSong.al?.picUrl)
                    .withString("songListName", firstSong.name)
                    .withString("athour", firstSong.ar?.firstOrNull()?.name)
                    .navigation(requireActivity())
            } else {
                // 没有歌曲时的提示
                Log.d("SingerSong", "没有可播放的歌曲")
                // 可以添加Toast提示：Toast.makeText(context, "没有可播放的歌曲", Toast.LENGTH_SHORT).show()
            }
        }

        // 初始化歌曲数量显示
        updateSongCount()
    }

    // 数据请求方法
    private fun fetchTopData() {
        val currentId = singerId
        if (currentId == null || currentId <= 0) {
            Log.e("SingerSong", "请求失败：ID无效（currentId=$currentId）")
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("SingerSong", "开始请求ID=$currentId")
                topViewModel.getTopData(currentId)
            } catch (e: Exception) {
                Log.e("SingerSong", "请求过程出错：${e.message}", e)
            }
        }
    }

    // 更新歌曲数量显示的方法
    private fun updateSongCount() {
        val count = topAdapter.currentList.size
        binding.topCount.text = "(${ count})"
        Log.d("SingerSong", "更新歌曲数量：$count")
    }
}
