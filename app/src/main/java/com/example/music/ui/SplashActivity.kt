package com.example.music.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.music.databinding.ActivitySplashBinding
import java.lang.ref.WeakReference

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = SplashHandler(Looper.getMainLooper())
    private val jumpDelay = 3000L // 延迟跳转时间（3秒）

    // Handler避免内存泄漏
    private class SplashHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // 弱引用
            val activityRef = msg.obj as WeakReference<SplashActivity>
            activityRef.get()?.let { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.jumpToMain()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        startAnimations()
        startAutoJump()
    }

    private fun initViews() {
        binding.stSkip.setOnClickListener {
            // 点击时取消自动跳转任务，保证主页只初始化一次
            handler.removeCallbacksAndMessages(null)
            jumpToMain()
        }
    }

    private fun startAnimations() {
        val ivLogo = binding.stImg
        val ivLogoText = binding.stText

        // 缩放动画
        val scaleAnim = ScaleAnimation(
            0.8f, 1f,
            0.8f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )

        // 淡入动画
        val alphaAnim = AlphaAnimation(0f, 1f)

        // 同时执行缩放和淡入，要是不组合的话，会被另一个覆盖掉
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(alphaAnim)
            duration = jumpDelay // 动画时长与跳转延迟一致
            fillAfter = true // 动画结束后保持最终状态
        }

        // 启动
        ivLogo.startAnimation(animationSet)
        ivLogoText.startAnimation(animationSet)
    }

    private fun startAutoJump() {
        // 发送延迟跳转消息
        val msg = Message.obtain()
        msg.obj = WeakReference(this)
        handler.sendMessageDelayed(msg, jumpDelay)
    }

    // 统一的跳转逻辑
    private fun jumpToMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java))
            // 添加页面过渡动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish() // 销毁启动页，避免返回
        } catch (e: Exception) {
            e.printStackTrace()
            finish() // 异常时确保关闭启动页
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //避免重复跳转和内存泄漏
        handler.removeCallbacksAndMessages(null)
    }
}