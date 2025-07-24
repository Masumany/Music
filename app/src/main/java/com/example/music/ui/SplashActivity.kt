package com.example.music.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.music.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val iv_logo = binding.stImg
        val iv_logo_text = binding.stText
        val skip= binding.stSkip

        skip.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val scaleAnim = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 3000
            fillAfter = true
        }

        // 淡入动画
        val alphaAnim = AlphaAnimation(0f, 1f).apply {
            duration = 3000
            fillAfter = true
        }

        // 同时播放两个动画
        iv_logo.startAnimation(scaleAnim)
        iv_logo.startAnimation(alphaAnim)

        iv_logo_text.startAnimation(scaleAnim)
        iv_logo_text.startAnimation(alphaAnim)

        // 延迟3秒后跳转到主页面
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
    