package com.example.music

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.module_musicplayer.MusicPlayService
import com.example.module_recommened.RecommendFragment
import com.example.module_recommened.adapter.LiAdapter
import com.example.module_recommened.viewmodel.ListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.therouter.router.Route
import kotlinx.coroutines.launch
import android.content.res.Resources
import androidx.cardview.widget.CardView
import com.example.lib.base.Song
import com.example.yourproject.converter.DataConverter

@Route(path = "/main/main")
class MainActivity : AppCompatActivity() {

    private lateinit var topButton: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var bottomMusicController: BottomMusicController
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var musicIv: ImageView
    private var rotationAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        initViews()
        initMainFragment()
        initBottomSheetDialog()
        initMusicService()
        initRotationAnimation()
    }

    private fun initViews() {
        topButton = findViewById(R.id.drawerButton)
        drawerLayout = findViewById(R.id.mainDrawerLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        musicIv = findViewById(R.id.music)

        // 侧边栏开关逻辑
        topButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun initMainFragment() {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainContent, RecommendFragment())
                .commit()
        } catch (e: Exception) {
            Log.e("MainActivity", "加载主Fragment失败: ${e.message}", e)
        }
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
                    val convertedSongs = DataConverter.convertBaseSongList(originalSongs)
                    songAdapter.submitList(convertedSongs) // 提交数据
                } else {
                    Toast.makeText(this@MainActivity, "数据错误: ${result.code}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "列表加载异常", e)
                Toast.makeText(this@MainActivity, "加载失败，请检查网络", Toast.LENGTH_SHORT).show()
            }
        }

        // 弹窗显示时调整高度
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.post {
                bottomSheet.layoutParams.height = 400.dpToPx() // 固定高度400dp
                bottomSheet.requestLayout() // 强制刷新布局
            }
        }

        findViewById<ImageView>(R.id.iv_more).setOnClickListener {
            if (!bottomSheetDialog.isShowing) {
                bottomSheetDialog.show()
            }
        }
    }

    private fun initMusicService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // 服务连接成功时回调
                val binder = service as MusicPlayService.MusicBinder
                val musicService = binder.service
                // 初始化音乐控制器
                bottomMusicController.onServiceConnected(musicService)
                // 同步动画状态（根据播放状态启动/停止）
                updateAnimationState(musicService.isPlaying)

                musicService.setOnPlayStateChanged { isPlaying, duration ->
                    updateAnimationState(isPlaying)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // 服务意外断开时回调
                Log.w("MainActivity", "音乐服务已断开连接")
            }
        }

        val bottomBarView = findViewById<CardView>(R.id.player)
        bottomMusicController = BottomMusicController(this, bottomBarView, serviceConnection)

        val musicServiceIntent = Intent(this, MusicPlayService::class.java)
        startService(musicServiceIntent) // 启动服务
        bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE) // 绑定服务
    }


    private fun initRotationAnimation() {
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_player).apply {
            interpolator = LinearInterpolator() // 匀速旋转
            duration = 20000 // 旋转周期：20秒/圈
            repeatCount = Animation.INFINITE // 无限循环
        }
    }



    fun updateAnimationState(isPlaying: Boolean) {
        if (isPlaying) {
            if (rotationAnimation == null) {
                initRotationAnimation() // 确保动画已初始化
            }
            if (!rotationAnimation!!.hasStarted() || rotationAnimation!!.hasEnded()) {
                musicIv.startAnimation(rotationAnimation)
            }
        } else {
            // 暂停/停止：立即停止动画并重置状态
            musicIv.clearAnimation() // 清除动画
        }
    }


    private fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density + 0.5f).toInt()
    }


    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务
        try {
            unbindService(serviceConnection)
        } catch (e: Exception) {
            Log.e("MainActivity", "服务解绑失败: ${e.message}", e)
        }
        // 销毁控制器
        bottomMusicController.onDestroy()
        // 停止动画
        musicIv.clearAnimation()
        // 关闭弹窗
        if (bottomSheetDialog.isShowing) {
            bottomSheetDialog.dismiss()
        }
    }
}