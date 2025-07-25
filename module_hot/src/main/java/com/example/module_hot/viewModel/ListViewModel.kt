package com.example.module_hot.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.module_hot.bean.list.Item0
import com.example.module_hot.bean.list.ListData
import com.example.module_hot.bean.list.ListItem
import com.example.module_hot.bean.list_songs.ListSongsData
import com.example.module_hot.bean.list_songs.Song
import com.example.module_hot.repository.NetRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListViewModel : ViewModel() {
    // 歌单列表（第一个请求结果）
    private val _listFirst = MutableStateFlow<List<Item0>>(emptyList())
    val listFirst: StateFlow<List<Item0>> = _listFirst.asStateFlow()

    // 歌单+歌曲整合数据（第二个请求结果，供UI展示）
    private val _listSongsData = MutableStateFlow<List<ListItem>>(emptyList())
    val listSongsData: StateFlow<List<ListItem>> = _listSongsData.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Init)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    fun loadListData(){
        if (_loadState.value == LoadState.Loading){
             return
        }

        viewModelScope.launch{
            try {
                _loadState.value = LoadState.Loading
                val response = NetRepository.apiService.getList()
                Log.d("ListViewModel", "接口请求结果：${response.message()}")

                if (response.isSuccessful) {
                    val data = response.body() as? ListData
                    if (data != null && data.code == 200) {
                        // 提取有效歌单列表（过滤无Id的无效项）
                        val firstList = data.list.filter { it.id > 0 }
                        if (firstList.isEmpty()){
                            _loadState.value = LoadState.Error("获取失败：数据为空")
                        }
                        _loadState.value = LoadState.Success
                        _listFirst.value = firstList
                        loadSongs()
                    } else {
                        _loadState.value = LoadState.Error("获取失败：数据为空")
                    }
                } else {
                    _loadState.value = LoadState.Error("请求失败：${response.code()}")
                    Log.e("ListViewModel", "获取失败：${response.code()}")
                }
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("获取异常 ${e.message}")
                Log.e("ListViewModel", "获取异常 ${e.message}")
                e.printStackTrace()
            }
        }
    }
    private fun loadSongs(){
        if (_loadState.value == LoadState.Loading){
            return
        }

        val firstList = _listFirst.value
        if (firstList.isEmpty()) {
            Log.e("ListViewModel", "无歌单数据，无法加载歌曲")
            return
        }

        viewModelScope.launch {
            try {
                _loadState.value = LoadState.Loading
                // 并发请求所有歌单的歌曲
                val deferredList = firstList.map { item ->
                    async { // 每个请求用async，实现并行
                        try {
                            val responseSongs = NetRepository.apiService.getListSongs(item.id.toString())
                            if (responseSongs.isSuccessful) {
                                val songData = responseSongs.body() as? ListSongsData
                                if (songData != null && songData.code == 200) {
                                    val songs = songData.songs
                                    val firstSong = if (songs.isNotEmpty()) formatSong(songs[0]) else "无歌曲"
                                    val secondSong = if (songs.size >= 2) formatSong(songs[1]) else ""
                                    val thirdSong = if (songs.size >= 3) formatSong(songs[2]) else ""
                                    val coverUrl = if (songs.isNotEmpty()) songs[0].al.picUrl else item.coverImgUrl
                                    ListItem(
                                        id = item.id,
                                        listName = item.name,
                                        listUpdateTime = item.updateTime,
                                        listCoverUrl = coverUrl,
                                        firstSongText = firstSong,
                                        secondSongText = secondSong,
                                        thirdSongText = thirdSong,
                                        songCoverUrl = coverUrl
                                    )
                                } else {
                                    createErrorListItem(item, "歌曲数据无效")
                                }
                            } else {
                                createErrorListItem(item, "歌曲请求失败:${responseSongs.code()}")
                            }
                        } catch (e: Exception) {
                            createErrorListItem(item, "请求异常:${e.message}")
                        }
                    }
                }
                // 等待所有并行请求完成，获取结果列表
                val uiItems = deferredList.awaitAll()
                _listSongsData.value = uiItems
                _loadState.value = LoadState.Success
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("歌曲加载异常：${e.message ?: "未知错误"}")
                e.printStackTrace()
            }
        }
    }

    // 辅助方法：创建错误状态的ListItem
    private fun createErrorListItem(list: Item0, errorMsg: String): ListItem {
        return ListItem(
            id = list.id, // 从Item0中获取歌单ID
            listName = list.name,
            listUpdateTime = list.updateTime,
            listCoverUrl = list.coverImgUrl,  // 用歌单默认封面
            firstSongText = errorMsg,
            secondSongText = "",  // 空字符串而非空格
            thirdSongText = "",
            songCoverUrl = list.coverImgUrl
        )
    }

    // 格式化歌曲为“歌名 — 歌手”（歌手列表用“/ ”分隔）
    private fun formatSong(song: Song): String {
        val artists = song.ar.joinToString("/ ") { it.name }  // 从Ar的name字段获取歌手名
        return "${song.name} — $artists"  // 歌曲名（Song的name）+ 歌手
    }
}