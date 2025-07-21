package com.example.module_hot.bean.list

data class ArtistToplist(
    val artists: List<Artist>,
    val coverUrl: String,
    val name: String,
    val position: Int,
    val upateFrequency: String,
    val updateFrequency: String
)