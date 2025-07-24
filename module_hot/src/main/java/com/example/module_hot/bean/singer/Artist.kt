package com.example.module_hot.bean.singer

import kotlin.collections.List
data class Artist(
    val albumSize: Int,
    val alias: List<String>,
    val briefDesc: String,
    val id: Int,
    val img1v1Id: Long,
    val img1v1Id_str: String,
    val img1v1Url: String,
    val lastRank: Int,
    val musicSize: Int,
    val name: String,
    val picId: Long,
    val picId_str: String,
    val picUrl: String,
    val relatedRes: RelatedRes,
    val score: Int,
    val topicPerson: Int,
    val trans: String,
    val transNames: List<String>
)