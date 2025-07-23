package com.example.moudle_search.bean.search

data class Resource(
    val mlogBaseData: MlogBaseData,
    val mlogExtVO: MlogExtVO,
    val shareUrl: String,
    val status: Int,
    val userProfile: Any
)