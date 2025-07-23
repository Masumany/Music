package com.example.moudle_search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
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
                val intent = Intent(this, SearchResultActivity::class.java)
                intent.putExtra("keyWord", hot.first)
                startActivity(intent)
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

        initClick()
        loadHotData()
        loadKeyWord()
        initEditTextListener()
    }
    private fun initClick() {
        binding.searchBack.setOnClickListener {
            finish()
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
    private fun search(keyWord: String) {
        Toast.makeText(this, "搜索：$keyWord", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra("keyWord", keyWord)
        startActivity(intent)
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
        // 监听搜索按钮
        binding.searchButton.setOnEditorActionListener { _, actionId, _ ->
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
        viewModel.getSuggestion(keyWord)
    }
}