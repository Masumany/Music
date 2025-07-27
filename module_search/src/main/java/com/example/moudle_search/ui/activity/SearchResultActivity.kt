package com.example.moudle_search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.moudle_search.R
import com.example.moudle_search.adapter.SearchResultAdapter
import com.example.moudle_search.databinding.ActivitySearchResultBinding

class SearchResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultBinding
    private var searchAdapter: SearchResultAdapter? = null
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
        searchAdapter = SearchResultAdapter(this, keywords)
        binding.searchViewPager.adapter = searchAdapter

        // 导航与ViewPager2联动
        binding.searchBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_song -> binding.searchViewPager.currentItem = 0
                R.id.menu_item_singer -> binding.searchViewPager.currentItem = 1
                R.id.menu_item_list -> binding.searchViewPager.currentItem = 2
                R.id.menu_item_mv -> binding.searchViewPager.currentItem = 3
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.searchViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.searchBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_song   // 页面 0 对应 song
                    1 -> R.id.menu_item_singer // 页面 1 对应 singer
                    2 -> R.id.menu_item_list   // 页面 2 对应 list
                    3 -> R.id.menu_item_mv     // 页面 3 对应 mv
                    else -> R.id.menu_item_song
                }
            }
        })
        binding.searchViewPager.offscreenPageLimit = 1  // 预加载
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
                returnToSearchActivity(keyword)
            }
        }

        // 添加搜索框点击事件：返回 SearchActivity
        binding.searchEt.setOnClickListener {
            val keyword = binding.searchEt.text.toString()
            returnToSearchActivity(keyword)
        }
    }
    private fun returnToSearchActivity(keyword: String) {
        val intent = Intent()
        intent.putExtra("keywords", keyword) // 携带当前关键词
        setResult(RESULT_OK, intent)
        finish() // 关闭当前页面，返回 SearchActivity
    }
    override fun onDestroy() {
        super.onDestroy()
        // 清理ViewPager2适配器，避免持有Fragment引用
        binding.searchViewPager.adapter = null
        searchAdapter = null
        // 移除回调监听
        binding.searchViewPager.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
    }
}