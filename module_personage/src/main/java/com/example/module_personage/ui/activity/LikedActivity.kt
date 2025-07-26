package com.example.module_personage.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_login_register.databinding.ActivityLikedBinding
import com.example.module_personage.adapter.LikedAdapter
import com.example.module_personage.bean.liked.Followed
import com.example.module_personage.viewModel.LikedViewModel
import com.example.module_personage.viewModel.LoadState

class LikedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikedBinding
    private lateinit var viewModel: LikedViewModel
    private val adapter by lazy{
        LikedAdapter(
            onItemClick = { followed: Followed ->
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
        binding = ActivityLikedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LikedViewModel::class.java]
        binding.rvLiked.adapter = adapter
        binding.rvLiked.layoutManager = LinearLayoutManager(this)

        initClick()
        loadLikedData()
    }
    private fun initClick(){
        binding.likedBack.setOnClickListener {
            finish()
        }
    }
    private fun loadLikedData(){
        val uid = intent.getIntExtra("uid", 0)
        viewModel.getLikedData(uid)

        lifecycleScope.launchWhenStarted {
            viewModel.likedData.collect{
                adapter.submitList(it.followeds)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbLiked.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbLiked.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbLiked.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbLiked.visibility = android.view.View.GONE
                        Toast.makeText(this@LikedActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbLiked.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}