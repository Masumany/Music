package com.example.module_personage.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_login_register.databinding.ActivityLikeBinding
import com.example.module_personage.adapter.LikeAdapter
import com.example.module_personage.bean.like.Follow
import com.example.module_personage.viewModel.LikeViewModel
import com.example.module_personage.viewModel.LoadState

class LikeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikeBinding
    private lateinit var viewModel: LikeViewModel
    private val adapter by lazy{
        LikeAdapter(
            onItemClick = { follow: Follow ->
                // 跳转主页
            },
//            onItemLikeClick = { follow: Follow ->
//                //接口
//                val currentList = adapter.currentList.toList()
//                val newList = currentList.filter {
//                    it.userId != follow.uerId
//                }
//                adapter.submitList(newList)
//            }
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LikeViewModel::class.java]
        binding.rvLike.adapter = adapter
        binding.rvLike.layoutManager = LinearLayoutManager(this)

        loadLikeData()
    }
    private fun loadLikeData(){
        val uid = intent.getIntExtra("uid", 0)
        viewModel.getLikeData(uid)

        lifecycleScope.launchWhenStarted {
            viewModel.likeData.collect{
                adapter.submitList(it.follow)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbLike.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbLike.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbLike.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbLike.visibility = android.view.View.GONE
                        Toast.makeText(this@LikeActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbLike.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}