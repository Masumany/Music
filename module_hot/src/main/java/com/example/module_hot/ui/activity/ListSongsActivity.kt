package com.example.module_hot.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_hot.adapter.ListSongsAdapter
import com.example.module_hot.bean.list_songs.Song
import com.example.module_hot.databinding.ActivityListSongsBinding
import com.example.module_hot.viewModel.ListSongsViewModel
import com.example.module_hot.viewModel.LoadState
import com.therouter.TheRouter

class ListSongsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListSongsBinding
    private lateinit var viewModel: ListSongsViewModel

    private val adapter by lazy{
        ListSongsAdapter(
            onItemClick = { song: Song ->
                TheRouter.build("/module_musicplayer/musicplayer")
                    .withString("id", song.id.toString())
                    .withString("songListName", song.name)
                    .withString("athour", song.ar[0].name)
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

        initClick ()
        loadListSongsData()
    }

    private fun initClick() {
        binding.songsListBack.setOnClickListener {
            finish()
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