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
import Adapter.TopAdapter
import com.example.module_details.databinding.FragmentSingersongBinding
import kotlinx.coroutines.launch
import viewmodel.TopViewModel


class SingerSongFragment : Fragment() {
    private lateinit var binding: FragmentSingersongBinding
    private lateinit var topList: RecyclerView
    private lateinit var topViewModel: TopViewModel
    private var singerId: Long? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingersongBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 接收歌手ID的方法（核心）
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
        // 1. 初始化RecyclerView（绑定布局中的ID，确保与XML一致）
        topList = binding.topRv // 确认XML中RecyclerView的ID是topRv
        topList.layoutManager = LinearLayoutManager(requireContext())
        val topAdapter = TopAdapter()
        topList.adapter = topAdapter
        Log.d("SingerSong", "RecyclerView初始化完成")

        // 2. 初始化ViewModel
        topViewModel = ViewModelProvider(this)[TopViewModel::class.java]
        Log.d("SingerSong", "ViewModel初始化完成")

        // 3. 观察数据变化
        topViewModel.topData.observe(viewLifecycleOwner) { result ->
            Log.d("SingerSong", "观察到数据变化：${result?.songs?.size ?: 0}首歌")
            if (result != null && result.songs.isNotEmpty()) {
                topAdapter.submitList(result.songs)
                Log.d("SingerSong", "数据更新成功，共${result.songs.size}首歌")
            } else {
                Log.d("SingerSong", "数据为空或歌曲列表为空")
            }
        }

        // 4. 若已接收ID，立即请求数据
        if (singerId != null) {
            Log.d("SingerSong", "onViewCreated中已获取ID=$singerId，开始请求数据")
            fetchTopData()
        } else {
            Log.d("SingerSong", "onViewCreated中未获取到ID，等待setSingerId调用")
        }
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
                topViewModel.getTopData(currentId) // 调用ViewModel请求数据
            } catch (e: Exception) {
                Log.e("SingerSong", "请求过程出错：${e.message}", e)
            }
        }
    }
}