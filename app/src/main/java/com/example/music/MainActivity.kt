package com.example.music

import Adapter.Vp2Adapter
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.lib.base.Song
import com.example.module_musicplayer.MusicPlayService
import com.example.module_recommened.RecommendFragment
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.viewmodel.ListViewModel
import com.example.music.databinding.ActivityHeaderBinding
import com.example.music.databinding.ActivityMainBinding
import com.example.music.databinding.NavHeaderBinding
import com.example.yourproject.converter.DataConverter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.therouter.router.Route
import kotlinx.coroutines.launch
import android.content.res.Resources
import androidx.cardview.widget.CardView

@Route(path = "/main/main")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: NavHeaderBinding
    private lateinit var topButton: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var bottomMusicController: BottomMusicController
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var musicIv: ImageView
    private var rotationAnimation: Animation? = null
    private var vpAdapter: Vp2Adapter? = null
    private lateinit var viewPager: ViewPager2 // 声明ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge() // 启用边缘到边缘显示

        initViews()
        initViewPager() // 优先初始化ViewPager2
        initHeaderView() // 拆分头部视图初始化
        initBottomSheetDialog()
        initMusicService()
        initRotationAnimation()
        setupBottomNavListener() // 底部导航联动逻辑
    }

    // 初始化头部视图及点击事件
    private fun initHeaderView() {
        headerBinding = NavHeaderBinding.inflate(layoutInflater)
        binding.navigationView.addView(headerBinding.root)

        headerBinding.more.setOnClickListener {
            startActivity(Intent(this, HeaderActivity::class.java))
        }

        headerBinding.backlogin.setOnClickListener {
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        }
    }

    // 初始化ViewPager2及适配器
    private fun initViewPager() {
        viewPager = binding.mainContent // 绑定布局中的ViewPager2
        vpAdapter = Vp2Adapter(this)
        viewPager.adapter = vpAdapter
        // 禁用预加载（可选，根据需求设置）
        viewPager.offscreenPageLimit = 2
    }

    private fun initViews() {
        topButton = binding.drawerButton // 直接使用binding获取视图
        drawerLayout = binding.mainDrawerLayout
        bottomNavigationView = binding.bottomNavigationView
        musicIv = binding.music

        // 侧边栏开关逻辑
        topButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    // 底部导航与ViewPager2联动
    private fun setupBottomNavListener() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> viewPager.currentItem = 0
                R.id.action_hot -> viewPager.currentItem = 1
                R.id.action_mine -> viewPager.currentItem = 2
            }
            true
        }

        // ViewPager2滑动时同步底部导航选中状态
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun initBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.fragment_musicplayerlist, null)
        bottomSheetDialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvlist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val songAdapter = LiAdapter()
        recyclerView.adapter = songAdapter

        val listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
        lifecycleScope.launch {
            try {
                val result = listViewModel.getListData(1, 5)
                if (result.code == 200) {
                    val originalSongs: List<Song> = result.data?.dailySongs ?: emptyList()
                    songAdapter.submitList(DataConverter.convertBaseSongList(originalSongs))
                } else {
                    Toast.makeText(this@MainActivity, "数据错误: ${result.code}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "列表加载异常", e)
                Toast.makeText(this@MainActivity, "加载失败，请检查网络", Toast.LENGTH_SHORT).show()
            }
        }

        // 弹窗高度设置
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.post {
                bottomSheet.layoutParams.height = 400.dpToPx()
                bottomSheet.requestLayout()
            }
        }

        binding.ivMore.setOnClickListener { // 使用binding获取视图
            if (!bottomSheetDialog.isShowing) {
                bottomSheetDialog.show()
            }
        }
    }

    private fun initMusicService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MusicPlayService.MusicBinder
                val musicService = binder.service
                bottomMusicController.onServiceConnected(musicService)
                updateAnimationState(musicService.isPlaying)

                musicService.setOnPlayStateChanged { isPlaying, _ ->
                    updateAnimationState(isPlaying)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w("MainActivity", "音乐服务已断开连接")
            }
        }

        val bottomBarView = binding.player
        bottomMusicController = BottomMusicController(this, bottomBarView, serviceConnection)

        val musicServiceIntent = Intent(this, MusicPlayService::class.java)
        startService(musicServiceIntent)
        bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun initRotationAnimation() {
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_player).apply {
            interpolator = LinearInterpolator()
            duration = 20000
            repeatCount = Animation.INFINITE
        }
    }

    fun updateAnimationState(isPlaying: Boolean) {
        if (isPlaying) {
            if (rotationAnimation == null) initRotationAnimation()
            if (!rotationAnimation!!.hasStarted() || rotationAnimation!!.hasEnded()) {
                musicIv.startAnimation(rotationAnimation)
            }
        } else {
            musicIv.clearAnimation()
        }
    }

    // dp转px工具方法
    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务
        try {
            unbindService(serviceConnection)
        } catch (e: Exception) {
            Log.e("MainActivity", "服务解绑失败: ${e.message}", e)
        }
        bottomMusicController.onDestroy()
        musicIv.clearAnimation()
        if (bottomSheetDialog.isShowing) {
            bottomSheetDialog.dismiss()
        }
    }
}