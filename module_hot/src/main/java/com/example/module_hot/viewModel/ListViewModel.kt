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
                val uiItems = mutableListOf<ListItem>()
                for (item in firstList){
                    val responseSongs = NetRepository.apiService.getListSongs(item.id.toString())
                    Log.d("ListViewModel", "接口请求结果：${responseSongs.message()}")

                    if (responseSongs.isSuccessful) {
                        val songData = responseSongs.body() as? ListSongsData
                        if (songData != null && songData.code == 200) {
                            val songs = songData.songs
                            // 格式化前3首歌曲（处理不足3首的情况）
                            val firstSong = if (songs.isNotEmpty()) formatSong(songs[0]) else "无歌曲"
                            val secondSong = if (songs.size >= 2) formatSong(songs[1]) else ""
                            val thirdSong = if (songs.size >= 3) formatSong(songs[2]) else ""
                            //优先用第一首歌的封面，无则用歌单默认封面（Item0的coverImgUrl）
                            val coverUrl = if (songs.isNotEmpty()) songs[0].al.picUrl else item.coverImgUrl

                            // 创建ListItem
                            val listItem = ListItem(
                                id = item.id, // 从Item0中获取歌单ID
                                listName = item.name,  // 歌单名（Item0的name字段）
                                listUpdateTime = item.updateTime,  // 歌单更新时间（Item0的updateTime）
                                listCoverUrl = coverUrl,  // 封面URL（优先歌曲封面）
                                firstSongText = firstSong,  // 第一首歌曲
                                secondSongText = secondSong,  // 第二首歌曲
                                thirdSongText = thirdSong,  // 第三首歌曲
                                songCoverUrl = coverUrl
                            )
                            uiItems.add(listItem)
                        } else {
                            uiItems.add(createErrorListItem(item, "歌曲数据无效"))
                        }
                    } else {
                        // 歌曲请求失败（如404）
                        uiItems.add(createErrorListItem(item, "歌曲请求失败"))
                        Log.e("ListViewModel", "歌单[${item.id}]歌曲请求失败：${responseSongs.code()}")
                    }
                }

                // 更新UI数据
                _listSongsData.value = uiItems
                _loadState.value = LoadState.Success
            } catch (e: Exception) {
                _loadState.value = LoadState.Error("歌曲加载异常：${e.message ?: "未知错误"}")
                Log.e("ListViewModel", "歌曲加载异常", e)
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