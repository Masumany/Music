package com.example.module_details

import SongViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_details.databinding.ActivitySongBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import kotlinx.coroutines.launch

@Route(path = "/song/SongActivity")
class SongActivity : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: Long = 0

    private lateinit var binding: ActivitySongBinding
    private lateinit var songViewModel: SongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TheRouter.inject(this)
        Log.d("SongActivity", "id: $id")

        // 1. 先初始化视图绑定（必须在使用binding前完成）
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 初始化ViewModel（只需要初始化一次）
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]

        binding.back.setOnClickListener {
            finish()
        }

        // 3. 初始化RecyclerView（设置布局管理器和初始适配器）
        initRecyclerView()

        // 4. 观察ViewModel的数据变化（必须在初始化ViewModel之后）
        observeMusicData()

        // 5. 加载数据
        loadData()
    }

    // 初始化RecyclerView
    private fun initRecyclerView() {
        // 设置水平布局管理器
        binding.mdRv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        // 初始设置空适配器（避免空指针）
        binding.mdRv.adapter = SongAdapter(emptyList())
    }

    // 观察歌曲数据
    private fun observeMusicData() {
        songViewModel.listMusicData.observe(this) { listMusicData ->
            Log.d("SongActivity", "收到数据：歌曲数量=${listMusicData.songs.size}")
            // 数据更新时刷新适配器
            if (listMusicData.songs.isNotEmpty()) {
                binding.mdRv.adapter = SongAdapter(listMusicData.songs)
            } else {
                Log.d("SongActivity", "无歌曲数据（可能网络请求失败）")
            }
        }
    }

    // 加载数据
    private fun loadData() {
        lifecycleScope.launch {
            if(id != 0L){
                songViewModel.getListMusicData(id) // 调用ViewModel的方法获取数据
            }else{
                Log.d("SongActivity", "无歌曲数据（可能网络请求失败）")
            }
        }
    }
}