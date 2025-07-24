package com.example.module_details


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.module_details.databinding.ActivitySingerBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import kotlinx.coroutines.launch
import viewmodel.SingerHomeViewModel

@Route(path = "/singer/SingerActivity")
class SingerActivity : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: Long = 0 // 从路由接收歌手ID

    private lateinit var binding: ActivitySingerBinding
    private lateinit var singerHomeViewModel: SingerHomeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TheRouter.inject(this) // 注入路由参数
        Log.d("SingerActivity", "id: $id")
        initFragment(savedInstanceState)


        binding.mdSingerback.setOnClickListener {
            finish()
        }

        // 初始化ViewModel
        singerHomeViewModel = ViewModelProvider(this)[SingerHomeViewModel::class.java]
        observeSingerHomeData()
        fetchSingerHomeData()


        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_singer -> {
                    switchFragment(SingerHomeFragment().apply { setSingerId(id.toLong()) })
                    true
                }
                R.id.action_songs -> {
                    val songFragment = SingerSongFragment()
                    Log.d("ForceCall", "准备手动调用setSingerId，id=$id")
                    songFragment.setSingerId(id) // 强制调用，确保执行
                    switchFragment(songFragment)
                    true
                }
                R.id.action_mv -> {
                    val songFragment = SingerMvFragment()
                    Log.d("ForceCall", "准备手动调用setSingerId，id=$id")
                    songFragment.setSingerId(id) // 强制调用，确保执行
                    switchFragment(songFragment)
                    true
                }
                else -> false
            }
        }
    }
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(binding.fragmentContainer.id, fragment)
            addToBackStack(null)
        }
    }

    private fun observeSingerHomeData() {
        singerHomeViewModel.singerHomeData.observe(this) { data ->
            if (data != null) {
                binding.singerName.text = data.data?.artist?.name ?: "未知歌手"
                Glide.with(this)
                    .load(data.data?.artist?.cover)
                    .into(binding.singerCover)
            }
        }
    }

    private fun fetchSingerHomeData() {
        singerHomeViewModel.viewModelScope.launch {
            singerHomeViewModel.getSingerHomeData(id)
        }
    }

    // 初始Fragment
    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val singerHomeFragment = SingerHomeFragment()
            singerHomeFragment.setSingerId(id)
            Log.d("SingerActivity", "已调用setSingerId，传递的id: $id")
            supportFragmentManager.commit {
                replace(binding.fragmentContainer.id, singerHomeFragment)
                addToBackStack(null)
            }
        }
    }
}
