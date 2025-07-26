package com.example.moudle_search.bean.list_songs

data class ListSongsData(
    val code: Int,
    val privileges: List<Privilege>,
    val songs: List<Song>
)