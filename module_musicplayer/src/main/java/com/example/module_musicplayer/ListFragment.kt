package com.example.module_musicplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lib.base.Song
import com.example.module_musicplayer.databinding.FragmentPlaylistBinding

class ListFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val songAdapter = PlayListAdapter()
    private var currentPlayList: List<Song> = emptyList()
    private var currentPlayingIndex = -1

    // 歌曲选择监听器
    private var onSongSelectListener: ((Int) -> Unit)? = null

    fun setOnSongSelectListener(listener: (Int) -> Unit) {
        onSongSelectListener = listener
    }

    fun updatePlayList(list: List<Song>, currentIndex: Int) {
        // 保存数据，无论视图是否初始化
        currentPlayList = list
        currentPlayingIndex = currentIndex

        // 检查视图是否已初始化
        if (_binding == null) {
            Log.w("ListFragment", "视图尚未初始化，暂不更新列表UI")
            return
        }

        songAdapter.updateData(list, currentIndex)

        if (currentIndex >= 0) {
            (_binding?.rvlist?.layoutManager as LinearLayoutManager)
                ?.scrollToPositionWithOffset(currentIndex, 200)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        // 初始状态设置为隐藏
        _binding?.root?.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initCloseButton()
        initAdapterListener()

        // 视图创建后，如果有缓存数据，立即更新
        if (currentPlayList.isNotEmpty()) {
            songAdapter.updateData(currentPlayList, currentPlayingIndex)
        }
    }

    // 控制Fragment显示/隐藏的方法
    fun setVisible(visible: Boolean) {
        _binding?.root?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // 初始化RecyclerView
    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvlist.layoutManager = layoutManager
        binding.rvlist.adapter = songAdapter
    }

    // 初始化适配器点击事件
    private fun initAdapterListener() {
        songAdapter.setOnItemClickListener { position ->
            onSongSelectListener?.invoke(position)
        }
    }

    // 关闭按钮
    private fun initCloseButton() {
        binding.mpImgBack.setOnClickListener {
            (activity as? MusicPlayerActivity)?.hideFragment()
        }
    }

    // 播放列表适配器
    inner class PlayListAdapter : RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {
        private var dataList: List<Song> = emptyList()
        private var currentIndex = -1
        private var itemClickListener: ((Int) -> Unit)? = null

        fun updateData(list: List<Song>, currentPos: Int) {
            dataList = list
            currentIndex = currentPos
            notifyDataSetChanged()
        }

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            itemClickListener = listener
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val songName: androidx.appcompat.widget.AppCompatTextView =
                itemView.findViewById(com.example.module_recommened.R.id.list_tv)
            val singer: androidx.appcompat.widget.AppCompatTextView =
                itemView.findViewById(com.example.module_details.R.id.list_tv1)
            val cover: androidx.appcompat.widget.AppCompatImageView =
                itemView.findViewById(com.example.module_details.R.id.list_img)

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener?.invoke(position)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.example.module_details.R.layout.item_songlist, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = dataList[position]
            holder.songName.text = song.name
            holder.singer.text = song.ar.joinToString { it.name }
            Glide.with(holder.itemView).load(song.al.picUrl).into(holder.cover)

            if (position == currentIndex) {
                holder.songName.setTextColor(holder.itemView.context.getColor(com.example.module_details.R.color.white))
                holder.singer.setTextColor(holder.itemView.context.getColor(com.example.module_details.R.color.white))
            } else {
                holder.songName.setTextColor(holder.itemView.context.getColor(R.color.black))
                holder.singer.setTextColor(holder.itemView.context.getColor(R.color.grey))
            }
        }

        override fun getItemCount() = dataList.size
    }

    // 释放binding引用，避免内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
