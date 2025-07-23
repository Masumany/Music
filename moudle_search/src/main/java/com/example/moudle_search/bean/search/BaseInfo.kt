package com.example.moudle_search.bean.search

data class BaseInfo(
    val alg: Any,
    val id: String,
    val matchField: Int,
    val matchFieldContent: Any,
    val mlogBaseDataType: Int,
    val position: Any,
    val reason: Any,
    val resource: Resource,
    val sameCity: Boolean,
    val type: Int
)