package com.example.module_login_register.ui.activity

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.module_login_register.viewModel.LoginState
import com.example.module_login_register.viewModel.QrCodeLoginViewModel
import com.example.module_login_register.R
import com.example.module_login_register.viewModel.QrCodeLoginViewModelFactory
import com.example.module_login_register.viewModel.QrLoadState
import com.therouter.TheRouter
import kotlinx.coroutines.launch

class QrCodeLoginActivity : AppCompatActivity() {
    private lateinit var refreshBtn: Button
    private lateinit var backBtn: Button
    private lateinit var qrCodeImg: ImageView
    private lateinit var stateText: TextView
    private val viewModel: QrCodeLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_login)
        refreshBtn = findViewById(R.id.refresh_button)
        backBtn = findViewById(R.id.qr_back)
        qrCodeImg = findViewById(R.id.qr_code_image)
        stateText = findViewById(R.id.state_textView)

        val sharedPreferences = getSharedPreferences("qr_code", Context.MODE_PRIVATE)
        val viewModel = ViewModelProvider(this, QrCodeLoginViewModelFactory(sharedPreferences))[QrCodeLoginViewModel::class.java]

        initClick()
        collectState()
        collectQrState()
        // 启动时自动请求二维码
        autoLoadQrCode()
    }

    // 新增：启动时自动加载二维码
    private fun autoLoadQrCode() {
        stateText.text = "正在获取二维码，请稍等..."
        qrCodeImg.setImageResource(R.drawable.loading) // 显示加载中图片
        viewModel.getQrCodeKey() // 触发网络请求
    }

    private fun initClick() {
        refreshBtn.setOnClickListener {
            stateText.text = "正在刷新，请稍等..."
            qrCodeImg.setImageResource(R.drawable.loading)
            viewModel.getQrCodeKey()
            stateText.text = "请扫码"
        }
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.loginState.collect{
                when(it){
                    is LoginState.Init -> {
                        stateText.text = "请扫码"
                    }
                    is LoginState.Loading -> {
                        stateText.text = "正在登录，请稍等..."
                    }
                    is LoginState.Success -> {
                        Toast.makeText(this@QrCodeLoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        TheRouter.build("/module_personage").navigation()
                    }
                    is LoginState.Error -> {
                        stateText.text = "登录失败,请刷新"
                    }
                }
            }
        }
    }
    private fun collectQrState() {
        lifecycleScope.launch {
            viewModel.qrState.collect {
                when (it) {
                    is QrLoadState.Init -> {
                        stateText.text = "请稍等"
                    }
                    is QrLoadState.Loading -> {
                        stateText.text = "正在加载二维码"
                    }
                    is QrLoadState.Success -> {
                        stateText.text = "请扫码登录"
                        Glide.with(this@QrCodeLoginActivity)
                            .load(viewModel.QrCodeData.value.data.qrimg)
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.error)
                            .into(qrCodeImg)
                    }
                    is QrLoadState.Error -> {
                        stateText.text = "二维码加载失败,请刷新"
                    }
                }
            }
        }
    }
}