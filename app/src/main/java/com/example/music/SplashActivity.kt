package com.example.music

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


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val iv_logo = findViewById<ImageView>(R.id.st_img)
        val iv_logo_text = findViewById<TextView>(R.id.st_text)
        val skip= findViewById<ImageView>(R.id.st_skip)

        skip.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val scaleAnim = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000
            fillAfter = true
        }

        // 淡入动画
        val alphaAnim = AlphaAnimation(0f, 1f).apply {
            duration = 2000
            fillAfter = true
        }

        // 同时播放两个动画
        iv_logo.startAnimation(scaleAnim)
        iv_logo.startAnimation(alphaAnim)

        iv_logo_text.startAnimation(scaleAnim)
        iv_logo_text.startAnimation(alphaAnim)

        // 延迟2秒后跳转到主页面
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 关闭
        }, 2000)
    }
}
    