package com.example.module_personage.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_login_register.R
import com.example.module_login_register.databinding.ActivityListSongsBinding
import com.example.module_personage.adapter.ListSongsAdapter
import com.example.module_personage.bean.Playlist
import com.example.module_personage.viewModel.ListSongsViewModel
import com.example.module_personage.viewModel.LoadState
import com.example.moudle_search.bean.list_songs.Song
import com.therouter.TheRouter

class ListSongsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListSongsBinding
    private lateinit var viewModel: ListSongsViewModel
    private var currentPlaylist: Playlist? = null

    private val adapter by lazy{
        ListSongsAdapter(
            onItemClick = { song: Song ->
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withString("songListName", song.name)
                    .withString("cover", song.al.picUrl)
                    .withLong("id", song.id)
                    .withString("athour", song.ar[0].name?: "未知")
                    .withLong("singerId", song.ar[0].id.toLong()?: 0)
                    .navigation(this)
                Log.d("ListSongsActivity", "${song.id},${song.name}, ${song.ar[0].name}")
            },
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvSongsList.adapter = adapter
        binding.rvSongsList.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this)[ListSongsViewModel::class.java]
        binding.tvSongsListTitle.text = intent.getStringExtra("name") ?: "未知歌单"
        // 初始化 SwipeRefreshLayout
        binding.songsListSwipeRefresh.apply {
            // 设置刷新动画的颜色
            setColorSchemeResources(R.color.black, R.color.white)

            // 设置下拉刷新的监听器
            setOnRefreshListener {
                // 下拉时触发数据刷新
                loadListSongsData()
            }
        }

        initClick ()
        loadListSongsData()
    }

    private fun initClick() {
        binding.songsListBack.setOnClickListener {
            finish()
        }
        binding.btnShare.setOnClickListener {
            Toast.makeText(this, "分享", Toast.LENGTH_SHORT).show()
            // 跳转到分享页面
            currentPlaylist?.let {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, "分享 歌单")
                    putExtra(Intent.EXTRA_TEXT, "发现一个超棒的歌单：https://music.163.com/playlist?id=${it.id}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "分享到..."))
            }
        }
    }
    private fun loadListSongsData() {
        val id = intent.getLongExtra("id", 0)
        Log.d("ListSongsActivity", "id: $id")
        if (id == 0L) {
            Toast.makeText(this, "id无效", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.getListSongsData(id)

        lifecycleScope.launchWhenStarted {
            viewModel.listSongsData.collect {
                adapter.submitList(it.songs)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbSongsList.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbSongsList.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbSongsList.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbSongsList.visibility = android.view.View.GONE
                        Toast.makeText(this@ListSongsActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbSongsList.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}