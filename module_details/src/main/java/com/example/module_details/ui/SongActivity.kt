package com.example.module_details

import Adapter.MusicDataCache
import Adapter.SongAdapter
import SongViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    @JvmField
    @Autowired
    var recommendCover: String = ""

    @JvmField
    @Autowired
    var recommendName: String = ""

    private lateinit var binding: ActivitySongBinding
    private lateinit var songViewModel: SongViewModel
    private var lastScrollY = 0  // 累计滚动距离
    private var albumTotalHeight = 0  // 专辑区总高度
    private var isFixedAtTop = false  // “播放全部”是否固定在顶部

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TheRouter.inject(this)
        Log.d("SongActivity", "id: $id")

        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]

        binding.back.setOnClickListener {
            finish()
        }
        initRecyclerView()
        observeMusicData()
        loadData()
        loadCover()
        setListeners()
        measureAlbumHeight()
        initScrollListener()
    }

    // 测量专辑区总高度
    private fun measureAlbumHeight() {
        binding.albumArea.post {
            // 获取专辑区自身高度
            val albumHeight = binding.albumArea.height
            // 获取专辑区的顶部margin
            val layoutParams =
                binding.albumArea.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            val marginTop = layoutParams.topMargin
            // 总高度 = 自身高度 + 顶部margin
            albumTotalHeight = albumHeight + marginTop
            Log.d("ScrollDebug", "专辑区总高度：$albumTotalHeight px")
        }
    }

    private fun initScrollListener() {
        binding.mdRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 专辑区高度未测量完成时，不执行滚动
                if (albumTotalHeight == 0) return

                // 更新累计滚动距离
                lastScrollY += dy
                if (lastScrollY < 0) lastScrollY = 0

                // 当滚动距离超过专辑区高度时，把它固定到顶部
                if (dy > 0 && lastScrollY > albumTotalHeight && !isFixedAtTop) {
                    fixPlayAreaAtTop()
                    isFixedAtTop = true
                }
                // 当滚动距离小于专辑区高度，恢复原位
                else if (dy < 0 && lastScrollY < albumTotalHeight && isFixedAtTop) {
                    restorePlayAreaPosition()
                    isFixedAtTop = false
                }
            }
        })
    }

    // “播放全部”区域固定在顶部
    private fun fixPlayAreaAtTop() {
        // 移动“播放全部”区域到顶部
        binding.fixedArea.animate()
            .translationY(-albumTotalHeight.toFloat())
            .setDuration(300)
            .start()

        // 隐藏专辑区
        binding.albumArea.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
    }

    // “播放全部”区域恢复到专辑区下方
    private fun restorePlayAreaPosition() {
        // 移动“播放全部”区域回原位置
        binding.fixedArea.animate()
            .translationY(0f)
            .setDuration(300)
            .start()

        //显示专辑区
        binding.albumArea.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun setListeners() {
        binding.mdAllstart.setOnClickListener {
            val list = MusicDataCache.currentSongList
            if (list != null && list.isNotEmpty()) {
                val firstSong = list[0]
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withLong("id", firstSong.id)
                    .withString("cover", firstSong.al.picUrl)
                    .withString("songListName", firstSong.name)
                    .withString("athour", firstSong.ar[0].name)
                    .withInt("currentPosition", 0)
                    .navigation(this)
            }
        }
    }

    private fun loadCover() {
        binding.mdTitle.text = recommendName
        Glide.with(this)
            .load(recommendCover)
            .error(R.drawable.md_music)
            .into(binding.mdImg)
    }

    private fun initRecyclerView() {
        binding.mdRv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.mdRv.adapter = SongAdapter(emptyList())
    }

    private fun observeMusicData() {
        songViewModel.listMusicData.observe(this) { listMusicData ->
            if (listMusicData == null || listMusicData.songs.isEmpty()) {
                Log.d("SongActivity", "无歌曲数据")
                return@observe
            }

            val count = songViewModel.listMusicData.value?.songs?.size
            binding.count.setText("(${count})")

            val limitedList = listMusicData.songs
            MusicDataCache.currentSongList = limitedList
            binding.mdRv.adapter = SongAdapter(limitedList)
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            if (id != 0L) {
                songViewModel.getListMusicData(id)
            } else {
                Log.d("SongActivity", "id为空，无法加载数据")
            }
        }
    }
}
