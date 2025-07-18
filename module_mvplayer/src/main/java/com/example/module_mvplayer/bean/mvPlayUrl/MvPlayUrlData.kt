package com.example.module_mvplayer.bean.mvPlayUrl

data class MvPlayUrlData(
    val id: Long, // MV唯一ID
    val url: String, // 播放地址
    val r: Int, // 分辨率
    val size: Long, // 文件大小
    val expi: Int, // 地址有效期
    val fee: Int, // 是否需要付费（0表示免费）
    val mvFee: Int // MV单独付费标识（0表示免费）
)
