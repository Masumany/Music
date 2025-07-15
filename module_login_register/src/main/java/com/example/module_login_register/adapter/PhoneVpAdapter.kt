package com.example.module_login_register.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.module_login.ui.fragment.AuthCodeLoginFragment
import com.example.module_login.ui.fragment.PasswordsLoginFragment

class PhoneVpAdapter (
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PasswordsLoginFragment()
            1 -> AuthCodeLoginFragment()
            else -> throw IllegalArgumentException("无效的登录页索引: $position")
        }
    }
}