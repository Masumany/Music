package com.example.moudle_search.bean.search

data class Artist(
    val albumSize: Int,
    val alg: String,
    val alias: List<Any>,
    val briefDesc: String,
    val fansSize: Int,
    val id: Int,
    val img1v1Id: Long,
    val img1v1Id_str: String,
    val img1v1Url: String,
    val musicSize: Int,
    val mvSize: Int,
    val name: String,
    val occupation: String,
    val officialTags: List<Any>,
    val picId: Long,
    val picId_str: String,
    val picUrl: String,
    val searchCircle: Any,
    val trans: String,
    val transNames: List<String>,
    val videoSize: Int
)