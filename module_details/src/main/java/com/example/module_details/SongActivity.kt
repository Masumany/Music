package com.example.module_details

import SongViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.module_details.databinding.ActivitySongBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import data.ListMusicData
import kotlinx.coroutines.launch

@Route(path = "/song/SongActivity")
class SongActivity : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: Long = 0

    @JvmField
    @Autowired
    var recommendCover:String= ""

    @JvmField
    @Autowired
    var recommendName:String= ""

    private lateinit var binding: ActivitySongBinding
    private lateinit var songViewModel: SongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val limitList: List<ListMusicData.Song>? = MusicDataCache.currentSongList
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
        TheRouter.inject( this)

        // 3. 初始化RecyclerView（设置布局管理器和初始适配器）
        initRecyclerView()

        // 4. 观察ViewModel的数据变化（必须在初始化ViewModel之后）
        observeMusicData()

        // 5. 加载数据
        loadData()

        loadCover()

        setListeners()


    }

    private fun setListeners() {
        binding.mdAllstart.setOnClickListener {
            val list=MusicDataCache.currentSongList
            Log.d("SongActivity", "setListeners: $list")
            if (list!=null){
                Log.d("SongActivity", "setListeners: ${list.size}")
                val router=TheRouter.build("/module_musicplayer/musicplayer")
                router.withString("cover",list[0].al.picUrl)
                router.withString("songListName",list[0].name)
                    .withString("athour",list[0].ar[0].name)
                router.withLong("id",list[0].id)
                    .navigation( this)
            }

        }
    }

    private fun loadCover() {
        val img=binding.mdImg
        val text=binding.mdTitle
        text.setText(recommendName)
        Glide.with(this)
            .load(recommendCover)
            .into(img)
    }

    // 初始化RecyclerView
    private fun initRecyclerView() {
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
            if (listMusicData == null || listMusicData.songs.isEmpty()) {
                Log.d("SongActivity", "无歌曲数据（可能网络请求失败）")
                return@observe
            }

            // 1. 限制列表为30首（与适配器逻辑一致）
            val limitedList = listMusicData.songs.take(30)

            // 2. 关键：将列表缓存到MusicDataCache，确保全局可访问
            MusicDataCache.currentSongList = limitedList
            Log.d("SongActivity", "数据缓存成功，列表大小：${limitedList.size}")

            // 3. 更新适配器
            binding.mdRv.adapter = SongAdapter(limitedList)
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