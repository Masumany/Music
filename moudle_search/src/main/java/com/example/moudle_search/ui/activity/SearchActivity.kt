package com.example.moudle_search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_search.adapter.SuggestionAdapter
import com.example.moudle_search.adapter.HotAdapter
import com.example.moudle_search.bean.searchHot.Hot
import com.example.moudle_search.bean.searchSuggestionData.AllMatch
import com.example.moudle_search.databinding.ActivitySearchBinding
import com.example.moudle_search.viewModel.LoadState
import com.example.moudle_search.viewModel.SearchViewModel

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private val adapter by lazy {
        HotAdapter (
            onItemClick = { hot: Hot ->
                search(hot.first)
            }
        )
    }
    private val suggestionAdapter by lazy {
        SuggestionAdapter (
            onItemClick = { match: AllMatch ->
                search(match.keyword)
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
        binding.suggestionRv.adapter = suggestionAdapter
        binding.suggestionRv.layoutManager = LinearLayoutManager(this)

        initClick()
        loadHotData()
        loadKeyWord()
        initEditTextListener()
    }
    private fun initClick() {
        binding.searchBack.setOnClickListener {
            finish()
        }
        binding.searchButton.setOnClickListener {
            val keyWord = binding.searchEt.text.toString()
            if (keyWord.isBlank()) {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show()
            } else {
                search(keyWord)
            }
        }
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
                        Toast.makeText(this@SearchActivity, "热搜加载失败", Toast.LENGTH_SHORT).show()
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
    private fun search(keyWord: String) {
        Toast.makeText(this, "搜索：$keyWord", Toast.LENGTH_SHORT).show()
        val searchLayout = binding.searchLayout  // 搜索页的搜索布局
        val searchEt = binding.searchEt  // 搜索框输入控件
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra("keywords", keyWord)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            searchLayout, // 搜索页的共享元素
            "search_box" // 与结果页布局的 transitionName 一致
        )
        startActivity(intent,options.toBundle())
    }
    private fun initEditTextListener() {
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 输入前回调
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 输入变化时实时获取文本
                val keyWord = s?.toString() ?: ""
                loadSuggestion(keyWord)
            }

            override fun afterTextChanged(s: Editable?) {
                // 输入完成后回调
            }
        })
        // 监听键盘搜索事件
        binding.searchEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyWord = binding.searchEt.text.toString()
                // 执行搜索行为
                search(keyWord)
                true
            } else {
                false
            }
        }
    }
    private fun loadSuggestion(keyWord: String) {
        if (keyWord.isBlank()) {
            binding.suggestionLayout.visibility = View.GONE
            binding.hotLayout.visibility = View.VISIBLE
            return
        }
        viewModel.getSuggestion(keyWord)
        lifecycleScope.launchWhenStarted {
            viewModel.suggestionData.collect{ list ->
                if (list.result.allMatch.isNotEmpty()) {
                    val sortedList = list.result.allMatch.sortedByDescending { it.keyword.length }
                    suggestionAdapter.submitList(sortedList)
                    binding.suggestionLayout.visibility = View.VISIBLE
                    binding.hotLayout.visibility = View.GONE
                } else {
                    binding.suggestionLayout.visibility = View.GONE
                    binding.hotLayout.visibility = View.VISIBLE
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.suggestionLoadState.collect{
                when(it){
                    is LoadState.Init -> {
                        binding.pbSuggestion.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.pbSuggestion.visibility = android.view.View.VISIBLE
                    }
                    is LoadState.Success -> {
                        binding.pbSuggestion.visibility = android.view.View.GONE
                    }
                    is LoadState.Error -> {
                        binding.pbSuggestion.visibility = android.view.View.GONE
                        Toast.makeText(this@SearchActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.pbSuggestion.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}