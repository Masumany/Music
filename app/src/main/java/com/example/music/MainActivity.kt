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

@Route(path = "/main/main")
class MainActivity : AppCompatActivity() {

    private lateinit var topButton: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var serviceConnection: ServiceConnection // 服务连接对象
    private lateinit var bottomMusicController: BottomMusicController // 音乐控制器
    private lateinit var bottomSheetDialog: BottomSheetDialog // 底部弹窗
    private lateinit var musicIv: ImageView // 唱片图标
    private var rotationAnimation: Animation? = null // 旋转动画

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 按依赖顺序初始化
        initViews()           // 初始化视图
        initMainFragment()    // 初始化主Fragment
        initBottomSheetDialog() // 初始化底部弹窗
        initMusicService()    // 初始化音乐服务（含serviceConnection）
        initRotationAnimation() // 初始化动画
    }

    /**
     * 初始化所有视图控件
     */
    private fun initViews() {
        topButton = findViewById(R.id.drawerButton)
        drawerLayout = findViewById(R.id.mainDrawerLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        musicIv = findViewById(R.id.music) // 绑定唱片图标

        // 侧边栏开关逻辑
        topButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    /**
     * 初始化主界面Fragment
     */
    private fun initMainFragment() {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainContent, RecommendFragment())
                .commit()
        } catch (e: Exception) {
            Log.e("MainActivity", "加载主Fragment失败: ${e.message}", e)
        }
    }

    /**
     * 初始化底部弹窗（播放列表）
     */
    private fun initBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.fragment_musicplayerlist, null)
        bottomSheetDialog.setContentView(dialogView)

        // 初始化弹窗中的RecyclerView
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvlist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val songAdapter = LiAdapter()
        recyclerView.adapter = songAdapter

        // 加载列表数据
        val listViewModel = ViewModelProvider(this)[ListViewModel::class.java]
        lifecycleScope.launch {
            try {
                val result = listViewModel.getListData(1, 5)
                if (result.code == 200) {
                    songAdapter.submitList(result.data?.dailySongs) // 提交数据
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

        // 绑定弹窗触发按钮（更多按钮）
        findViewById<ImageView>(R.id.iv_more).setOnClickListener {
            if (!bottomSheetDialog.isShowing) {
                bottomSheetDialog.show()
            }
        }
    }

    /**
     * 初始化音乐服务（核心修复：先初始化serviceConnection再使用）
     */
    private fun initMusicService() {
        // 1. 先初始化serviceConnection（必须在使用前赋值）
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

        // 2. 再创建音乐控制器（此时serviceConnection已初始化）
        val bottomBarView = findViewById<CardView>(R.id.player)
        bottomMusicController = BottomMusicController(this, bottomBarView, serviceConnection)

        // 3. 启动并绑定音乐服务
        val musicServiceIntent = Intent(this, MusicPlayService::class.java)
        startService(musicServiceIntent) // 启动服务
        bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE) // 绑定服务
    }

    /**
     * 初始化唱片旋转动画
     */
    private fun initRotationAnimation() {
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_player).apply {
            interpolator = LinearInterpolator() // 匀速旋转
            duration = 20000 // 旋转周期：20秒/圈
            repeatCount = Animation.INFINITE // 无限循环
        }
    }

    /**
     * 根据播放状态更新动画
     * @param isPlaying 是否正在播放
     */
    // MainActivity.kt 中完善动画更新方法
    fun updateAnimationState(isPlaying: Boolean) {
        if (isPlaying) {
            // 播放中：启动动画（如果未启动）
            if (rotationAnimation == null) {
                initRotationAnimation() // 确保动画已初始化
            }
            if (!rotationAnimation!!.hasStarted() || rotationAnimation!!.hasEnded()) {
                musicIv.startAnimation(rotationAnimation)
            }
        } else {
            // 暂停/停止：立即停止动画并重置状态
            musicIv.clearAnimation() // 清除动画（关键）
        }
    }

    /**
     * dp转px工具方法
     */
    private fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density + 0.5f).toInt()
    }

    /**
     * 生命周期：销毁时清理资源
     */
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