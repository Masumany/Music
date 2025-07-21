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
import androidx.recyclerview.widget.RecyclerView
import com.example.module_details.databinding.FragmentSingerhomeBinding
import com.therouter.router.Route
import kotlinx.coroutines.launch
import viewmodel.SimilarViewModel
import viewmodel.SingerHomeViewModel

@Route(path = "/singer/SingerHomeFragment")
class SingerHomeFragment : Fragment() {

    private lateinit var binding: FragmentSingerhomeBinding
    private lateinit var singerHomeViewModel: SingerHomeViewModel
    private lateinit var similarViewModel: SimilarViewModel
    private var singerId: Long? = null
    private var isRequesting = false
    // 修复：变量名改为小写开头（遵循规范）
    private val similarAdapter = SimilarAdapter(emptyList())
    private lateinit var similarList: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSingerhomeBinding.inflate(inflater, container, false)
        Log.d("SingerHomeBinding", "布局加载成功: ${binding.root.id}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        singerHomeViewModel = ViewModelProvider(requireActivity())[SingerHomeViewModel::class.java]
        similarViewModel = ViewModelProvider(requireActivity())[SimilarViewModel::class.java]
        observeData()

        similarList = binding.singerRv
        similarList.adapter = similarAdapter
        similarList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // 观察相似歌手数据变化（关键修复）
        observeSimilarData()

        // 已有ID则触发请求
        if (singerId != null) {
            fetchData()
            lifecycleScope.launch { // 修复：在协程中调用suspend函数
                fetchSimilarData(singerId)
            }
        }
    }

    // 新增：观察相似歌手数据的LiveData
    private fun observeSimilarData() {
        similarViewModel.similarData.observe(viewLifecycleOwner) { response ->
            if (response != null && response.artists.isNotEmpty()) {
                Log.d("SimilarData", "获取到${response.artists.size}个相似歌手")
                similarList.adapter = SimilarAdapter(response.artists) // 更新适配器
            } else {
                Log.d("SimilarData", "未获取到相似歌手数据")
            }
        }
    }

    // 修复：suspend函数的正确实现
    private suspend fun fetchSimilarData(singerId: Long?) {
        if (singerId == null || singerId <= 0) {
            Log.e("fetchSimilarData", "无效的歌手ID: $singerId")
            return
        }

        try {
            Log.d("fetchSimilarData", "开始请求相似歌手数据，ID=$singerId")
            // 先请求数据（suspend函数，必须在协程中调用）
            similarViewModel.getSimilarData(singerId)
            // 数据更新通过observeSimilarData处理，无需手动获取value
        } catch (e: Exception) {
            Log.e("fetchSimilarData", "请求失败", e)
        }
    }

    fun setSingerId(id: Long) {
        if (id <= 0) {
            Log.e("SingerHomeFragment", "setSingerId: 无效ID=$id")
            return
        }
        this.singerId = id
        Log.d("SingerHomeFragment", "setSingerId: 接收ID=$id")

        if (isAdded) {
            fetchData()
            // 修复：在协程中调用suspend函数
            lifecycleScope.launch {
                fetchSimilarData(singerId)
            }
        } else {
            Log.d("SingerHomeFragment", "Fragment未附加，等待onViewCreated后自动请求")
        }
    }

    // 观察歌手详情数据
    private fun observeData() {
        singerHomeViewModel.singerHomeData.observe(viewLifecycleOwner) { response ->
            if (!isAdded || !::binding.isInitialized) {
                Log.w("SingerHomeObserve", "Fragment未附加或视图未初始化")
                return@observe
            }

            Log.d("SingerHomeObserve", "接收到数据，code=${response?.code}")
            if (response?.code == 200 && response.data?.artist != null) {
                binding.mdSingercontent.text = response.data.artist.briefDesc ?: "暂无简介"
            } else {
                binding.mdSingercontent.text = "未找到歌手信息（code: ${response?.code ?: -1}）"
            }
            isRequesting = false
        }
    }

    private fun fetchData() {
        if (isRequesting) {
            Log.d("SingerHomeFetch", "已有请求在处理中，跳过重复请求")
            return
        }

        val currentId = singerId
        if (currentId == null || currentId <= 0) {
            Log.e("SingerHomeFetch", "无效的singerId: $currentId")
            return
        }

        if (!isAdded) {
            Log.w("SingerHomeFetch", "Fragment未附加，不发起请求")
            return
        }

        isRequesting = true
        lifecycleScope.launch {
            Log.d("SingerHomeFetch", "开始请求歌手ID: $currentId 的数据")
            singerHomeViewModel.getSingerHomeData(currentId)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次Fragment可见时，重新请求数据（利用已保存的singerId）
        if (singerId != null && singerId!! > 0) {
            Log.d("SingerHomeFragment", "Fragment可见，重新请求数据，ID=$singerId")
            fetchData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRequesting = false
        similarList.adapter = null // 避免内存泄漏
    }
}