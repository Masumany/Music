package com.example.module_login.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.module_login_register.viewModel.LoginState
import com.example.module_login_register.viewModel.PasswordsLoginViewModel
import com.example.module_login_register.R
import kotlinx.coroutines.launch

class PasswordsLoginFragment : Fragment() {
    private lateinit var loginBtn:  Button
    private lateinit var phoneEtv: EditText
    private lateinit var passwordEtv: EditText

    companion object {
        fun newInstance() = PasswordsLoginFragment()
    }

    private val viewModel: PasswordsLoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_passwords_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginBtn = view.findViewById(R.id.password_login)
        phoneEtv = view.findViewById(R.id.phone_number)
        passwordEtv = view.findViewById(R.id.password)

        initClick()
        collectLoginState()
    }
    private fun initClick() {
        loginBtn.setOnClickListener {
            val phone = phoneEtv.text.toString().trim()
            val password = passwordEtv.text.toString()
            viewModel.loginWithPassword(phone, password)
        }
    }
    private fun collectLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect{
                val context = requireContext()
                when(it){
                    is LoginState.Init -> {
                        loginBtn.isEnabled = true
                        loginBtn.text = "登录"
                    }
                    is LoginState.Loading -> {
                        loginBtn.isEnabled = false
                        loginBtn.text = "登录中..."
                    }
                    is LoginState.Success -> {
                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Error -> {
                        Toast.makeText( context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}