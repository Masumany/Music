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
import com.example.module_musicplayer.databinding.FragmentPlaylistBinding
import data.ListMusicData  // 导入新的Song类型所在包

class ListFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    // 1. 将列表类型改为新的Song类型
    private var currentPlayList: List<ListMusicData.Song> = emptyList()
    private var currentPlayingIndex = -1

    private val songAdapter = PlayListAdapter()
    private var onSongSelectListener: ((Int) -> Unit)? = null

    fun setOnSongSelectListener(listener: (Int) -> Unit) {
        onSongSelectListener = listener
    }

    // 2. 修改updatePlayList方法参数类型为新的Song列表
    fun updatePlayList(list: List<ListMusicData.Song>, currentIndex: Int) {
        currentPlayList = list
        currentPlayingIndex = currentIndex

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
        _binding?.root?.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initCloseButton()
        initAdapterListener()

        if (currentPlayList.isNotEmpty()) {
            songAdapter.updateData(currentPlayList, currentPlayingIndex)
        }
    }

    fun setVisible(visible: Boolean) {
        _binding?.root?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvlist.layoutManager = layoutManager
        binding.rvlist.adapter = songAdapter
    }

    private fun initAdapterListener() {
        songAdapter.setOnItemClickListener { position ->
            onSongSelectListener?.invoke(position)
        }
    }

    private fun initCloseButton() {
        binding.mpImgBack.setOnClickListener {
            (activity as? MusicPlayerActivity)?.hideFragment()
        }
    }

    // 3. 修改适配器以支持新的Song类型
    inner class PlayListAdapter : RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {
        // 适配器内部列表类型改为新的Song
        private var dataList: List<ListMusicData.Song> = emptyList()
        private var currentIndex = -1
        private var itemClickListener: ((Int) -> Unit)? = null

        // 更新数据的方法参数类型同步修改
        fun updateData(list: List<ListMusicData.Song>, currentPos: Int) {
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

        // 4. 绑定数据时使用新Song类型的属性（字段名需与新类型一致）
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = dataList[position]
            holder.songName.text = song.name  // 假设新类型有name字段
            holder.singer.text = song.ar.joinToString { it.name }  // 假设新类型有ar字段（歌手列表）
            Glide.with(holder.itemView).load(song.al.picUrl).into(holder.cover)  // 假设新类型有al字段（专辑信息）

            // 设置选中状态的颜色
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}