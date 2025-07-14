package com.example.module_login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.module_login.R

class LoginActivity : AppCompatActivity() {
    private lateinit var loginbtn: Button
    private lateinit var registerbtn: Button
    private lateinit var visitorbtn: Button
    private lateinit var mailboxbtn: Button
    private lateinit var qr_codebtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginbtn = findViewById(R.id.phone_login)
        registerbtn = findViewById(R.id.register)
        visitorbtn = findViewById(R.id.visitor_login)
        mailboxbtn = findViewById(R.id.mailbox_login)
        qr_codebtn = findViewById(R.id.qr_code_login)
        initClick ()
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
            val intent = Intent(this, VisitorLoginActivity::class.java)
            startActivity(intent)
        }
    }
}