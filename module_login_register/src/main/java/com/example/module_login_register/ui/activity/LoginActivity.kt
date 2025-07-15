package com.example.module_login_register.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.module_login_register.viewModel.LoginState
import com.example.module_login_register.viewModel.LoginViewModel
import com.example.module_login_register.R
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var loginbtn: Button
    private lateinit var registerbtn: Button
    private lateinit var visitorbtn: Button
    private lateinit var mailboxbtn: Button
    private lateinit var qr_codebtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        loginbtn = findViewById(R.id.phone_login)
        registerbtn = findViewById(R.id.register)
        visitorbtn = findViewById(R.id.visitor_login)
        mailboxbtn = findViewById(R.id.mailbox_login)
        qr_codebtn = findViewById(R.id.qr_code_login)
        initClick ()
        collectLoginState()
    }
    private fun initClick () {
        loginbtn.setOnClickListener {
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
        }
        registerbtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        mailboxbtn.setOnClickListener {
            val intent = Intent(this, MailboxLoginActivity::class.java)
            startActivity(intent)
        }
        qr_codebtn.setOnClickListener {
            val intent = Intent(this, QrCodeLoginActivity::class.java)
            startActivity(intent)
        }
        visitorbtn.setOnClickListener {
            viewModel.getVisitorLoginData()
        }
    }
    private fun collectLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect {
                when (it) {
                    is LoginState.Init -> {
                        //初始化
                    }
                    is LoginState.Loading -> {
                        //加载中
                        Toast.makeText(this@LoginActivity, "加载中...请稍后", Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Success -> {
                        //成功
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Error -> {
                        //失败
                        Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}