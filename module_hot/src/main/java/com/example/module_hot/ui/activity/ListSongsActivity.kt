package com.example.module_hot.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_hot.R
import com.example.module_hot.adapter.ListSongsAdapter
import com.example.module_hot.bean.list_songs.Song
import com.example.module_hot.databinding.ActivityListSongsBinding
import com.example.module_hot.viewModel.ListSongsViewModel
import com.example.module_hot.viewModel.LoadState

class ListSongsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListSongsBinding
    private lateinit var viewModel: ListSongsViewModel

    private val adapter by lazy{
        ListSongsAdapter(
            onItemClick = { song: Song ->
                // 跳转主页
            },
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvSongsList.adapter = adapter
        binding.rvSongsList.layoutManager = LinearLayoutManager(this)

        loadListSongsData()
    }
    private fun loadListSongsData() {
        val id = intent.getIntExtra("id", 0)
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