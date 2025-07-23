package com.example.moudle_search.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moudle_search.adapter.HotAdapter
import com.example.moudle_search.bean.searchHot.Hot
import com.example.moudle_search.databinding.ActivitySearchBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SearchViewModel

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private val adapter by lazy {
        HotAdapter (
            onItemClick = { hot: Hot ->

            }
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        binding.hotRv.adapter = adapter
        binding.hotRv.layoutManager = LinearLayoutManager(this)

        loadHotData()
        loadKeyWord()
    }
    private fun loadHotData() {
        viewModel.getHotData()

        lifecycleScope.launchWhenStarted {
            viewModel.hotData.collect{
                adapter.submitList(it.result.hots)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.hotLoadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbHot.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbHot.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbHot.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbHot.visibility = android.view.View.GONE
                        Toast.makeText(this@SearchActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbHot.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    private fun loadKeyWord() {
        viewModel.getKeyWord()

        lifecycleScope.launchWhenStarted {
            viewModel.keyWord.collect {
                binding.searchEt.hint = it.data.showKeyword
            }
        }
    }
}