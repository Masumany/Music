package com.example.moudle_search.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.moudle_search.R
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.databinding.ActivitySearchResultBinding

class SearchResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keywords = intent.getStringExtra("keywords")
        if (keywords.isNullOrBlank()) {
            Toast.makeText(this, "关键字为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // 设置搜索框文本
        binding.searchEt.setText(keywords)
        binding.searchEt.setSelection(keywords.length) // 光标移到末尾
        binding.searchViewPager.adapter = SearchResultAdapter(this,keywords)

        // 导航与ViewPager2联动
        binding.searchBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_song -> binding.searchViewPager.currentItem = 0
                R.id.menu_item_singer -> binding.searchViewPager.currentItem = 1
//                R.id.menu_item_list -> binding.searchViewPager.currentItem = 2
                R.id.menu_item_mv -> binding.searchViewPager.currentItem = 2
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.searchViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.searchBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_song   // 页面 0 对应 song
                    1 -> R.id.menu_item_singer // 页面 1 对应 singer
//                    2 -> R.id.menu_item_list   // 页面 2 对应 list
                    2 -> R.id.menu_item_mv     // 页面 3 对应 mv
                    else -> R.id.menu_item_song
                }
            }
        })
        binding.searchViewPager.offscreenPageLimit = 3  // 预加载3页（总4页，全部加载）
        initClick()
    }
    private fun initClick() {
        binding.searchBack.setOnClickListener {
            finish()
        }
        binding.searchButton.setOnClickListener {
            val keyword = binding.searchEt.text.toString()
            if (keyword.isBlank()) {
                Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show()
            } else {
                performNewSearch(keyword)
            }
        }
    }
    private fun performNewSearch(keyword: String) {
        Toast.makeText(this, "搜索：$keyword", Toast.LENGTH_SHORT).show()

        // 通知 Adapter 内部的 Fragment 更新搜索关键词
        val adapter = binding.searchViewPager.adapter
        if (adapter is SearchResultAdapter) {
            adapter.updateKeyword(keyword)
        }
    }
}