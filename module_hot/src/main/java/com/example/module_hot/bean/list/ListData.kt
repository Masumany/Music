package com.example.module_hot.bean.list

import com.google.gson.annotations.SerializedName

data class ListData(
    @SerializedName("code") val code: Int,
    // 歌单列表（核心数据）
    @SerializedName("list") val list: List<Item0>
)
data class Item0(
    // 歌单ID（用于跳转详情和分享）
    @SerializedName("id") val id: Long,
    // 歌单名称（UI展示）
    @SerializedName("name") val name: String,
    // 歌单封面URL（UI展示）
    @SerializedName("coverImgUrl") val coverImgUrl: String,
    // 歌单更新时间（UI展示）
    @SerializedName("updateTime") val updateTime: Long,
    // 歌单描述（可选，UI展示）
    @SerializedName("description") val description: String?
)