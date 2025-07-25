package com.example.music.ui

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
import com.example.music.R
import com.example.music.viewmodel.BottomViewModel
import com.therouter.TheRouter

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
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomViewModel: BottomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate:执行初始化")

        if(savedInstanceState!=null){
            return  //不重复初始化
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.searchView.setOnClickListener{
            TheRouter.build("/module_search/SearchActivity")
                .navigation( this)

        }

        initViews()
        initViewPager() // 初始化ViewPager2
        initHeaderView()
        initBottomSheetDialog() //初始化底部列表弹窗
        initMusicService()
        initRotationAnimation()  //初始化底部栏音乐播放器
        setupBottomNavListener() // 底部导航
    }


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
        viewPager = binding.mainContent
        vpAdapter = Vp2Adapter(this)
        viewPager.adapter = vpAdapter
        viewPager.setUserInputEnabled(false);
    }

    private fun initViews() {
        topButton = binding.drawerButton
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

    // 底部导航与ViewPager2
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

        binding.ivMore.setOnClickListener {
            if (!bottomSheetDialog.isShowing) {
                bottomSheetDialog.show()
            }
        }
    }

    private var isServiceBound = false
    private fun initMusicService() {
        if(isServiceBound) return
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MusicPlayService.MusicBinder
                val musicService = binder.service
                bottomViewModel.onServiceConnected(musicService)
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
        bottomMusicController = BottomMusicController(this, bottomBarView )

       bottomViewModel=ViewModelProvider(this)[BottomViewModel::class.java]
        bottomViewModel.initService( this)


        val musicServiceIntent = Intent(this, MusicPlayService::class.java)
        startService(musicServiceIntent)
        bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE)

        isServiceBound= true
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

    // dp转px
    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务：先检查serviceConnection是否已初始化
        if (::serviceConnection.isInitialized) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e("MainActivity", "服务解绑失败: ${e.message}", e)
            }
        }

        // 检查bottomMusicController是否已初始化
        if (::bottomMusicController.isInitialized) {
            bottomMusicController.onDestroy()
        }

        musicIv.clearAnimation()
        if (::bottomSheetDialog.isInitialized && bottomSheetDialog.isShowing) {
            bottomSheetDialog.dismiss()
        }
    }
}