package com.example.module_personage.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.module_login_register.R
import com.example.module_login_register.databinding.ActivityPersonageBinding
import com.example.module_personage.adapter.ViewPagerAdapter
import com.example.module_personage.viewModel.PersonageViewModel
import com.therouter.router.Route

@Route(path = "/module_personage")
class PersonageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonageBinding
    private val viewModel: PersonageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.personageViewPager.adapter = ViewPagerAdapter(this)

        // 导航与ViewPager2联动
        binding.personageBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_song_list -> binding.personageViewPager.currentItem = 0
                R.id.menu_item_history -> binding.personageViewPager.currentItem = 1
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.personageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.personageBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_song_list
                    1 -> R.id.menu_item_history
                    else -> R.id.menu_item_song_list
                }
            }
        })

        initClick()
    }
    private fun initClick() {
        binding.personageImageView.setOnClickListener {

        }
        binding.likeButton.setOnClickListener {
            val intent = Intent(this, LikeActivity::class.java)
            startActivity(intent)
        }
        binding.likedButton.setOnClickListener {
            val intent = Intent(this, LikedActivity::class.java)
            startActivity(intent)
        }
        binding.downloadButton.setOnClickListener {
            val intent = Intent(this, DownloadActivity::class.java)
            startActivity(intent)
        }
    }
}