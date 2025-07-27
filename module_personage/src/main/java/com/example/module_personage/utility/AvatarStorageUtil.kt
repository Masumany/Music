package com.example.module_personage.utility

import android.content.Context
import android.net.Uri
import android.content.SharedPreferences

object AvatarStorageUtil {
    // 存储文件名
    private const val PREF_NAME = "avatar_prefs"
    // 存储键名
    private const val AVATAR_URI_KEY = "avatar_uri"

    // 保存头像URI到本地
    fun saveAvatarUri(context: Context, uri: Uri) {
        // 获取SharedPreferences实例
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // 保存URI字符串（Uri不能直接存储，需转为字符串）
        prefs.edit().putString(AVATAR_URI_KEY, uri.toString()).apply()
    }

    // 从本地读取头像URI
    fun getAvatarUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // 读取字符串并转为Uri
        val uriString = prefs.getString(AVATAR_URI_KEY, null)
        return if (uriString.isNullOrEmpty()) null else Uri.parse(uriString)
    }

    // 清除本地存储的头像URI
    fun clearAvatarUri(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(AVATAR_URI_KEY).apply()
    }
}