package com.example.module_hot.bean.list_songs

data class ListSongsData(
    val code: Int,
    val privileges: List<Privilege>,
    val songs: List<Song>
)