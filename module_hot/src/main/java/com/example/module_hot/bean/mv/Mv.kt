package com.example.module_hot.bean.mv

data class Mv(
    val aliaName: String,
    val appTitle: String,
    val appword: String,
    val area: String,
    val artists: List<Artist>,
    val authId: Int,
    val caption: Int,
    val captionLanguage: String,
    val dayplays: Int,
    val desc: String,
    val fee: Int,
    val id: Int,
    val monthplays: Int,
    val mottos: String,
    val neteaseonly: Int,
    val oneword: Any,
    val online: Long,
    val pic16v9: Long,
    val pic4v3: Long,
    val plays: Int,
    val publishTime: String,
    val score: Int,
    val stars: Any,
    val status: Int,
    val style: Any,
    val subTitle: String,
    val subType: String,
    val title: String,
    val topWeeks: String,
    val transName: String,
    val type: String,
    val upban: Int,
    val valid: Int,
    val videos: List<Video>,
    val weekplays: Int
)