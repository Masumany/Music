package com.example.module_details

import Event.CloseLyricEvent
import Event.PlayProgressEvent
import Event.SongChangeEvent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.module_details.databinding.ActivitySongwordBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import data.SongWordData
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import viewmodel.SongWordViewModel

// 歌词行数据类
data class LyricLine(
    val timeMs: Long,
    val content: String
)

// 时间解析
object LyricTimeParser {
    fun parseTimeTag(timeTag: String): Long {
        return try {
            val timeStr = timeTag.replace(Regex("\\s+"), "").removeSurrounding("[", "]")  //去除[]
            val minSec = timeStr.split(":")
            val minutes = minSec[0].toLong()  //分割分钟和秒数
            val secMs = minSec[1].split(".")
            val seconds = secMs[0].toLong()//分割秒数和毫秒
            val milliseconds = if (secMs.size > 1) {  //让毫秒部分为3位，要不与它的数据类不匹配了
                secMs[1].padEnd(3, '0').take(3).toLong()
            } else {
                0
            }
            minutes * 60 * 1000 + seconds * 1000 + milliseconds  //总的毫秒数
        } catch (e: Exception) {
            Log.e("TimeParseError", "解析失败: $timeTag", e)
            0L
        }
    }
}

// 歌词适配器
class LyricAdapter : ListAdapter<LyricLine, LyricAdapter.LyricViewHolder>(LyricDiffCallback()) {

    private var currentLinePosition = -1  // 仅保留当前行高亮

    inner class LyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lyricText: TextView = itemView.findViewById(R.id.tv_lyric_content)

        fun bind(lyricLine: LyricLine, isCurrentLine: Boolean) {
            lyricText.text = lyricLine.content

            if (isCurrentLine) {
                lyricText.setTextColor(itemView.context.getColor(R.color.white))
                lyricText.textSize = 18f
                lyricText.alpha = 1.0f
            } else {
                // 未演唱或者是已经演唱了的歌词
                lyricText.setTextColor(itemView.context.getColor(R.color.commom))
                lyricText.textSize = 14f
                lyricText.alpha = 0.7f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric, parent, false)
        return LyricViewHolder(view)
    }

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        val lyricLine = getItem(position)
        holder.bind(lyricLine, position == currentLinePosition)
    }

    //更新当前播放的歌词的位置
    fun updateCurrentLine(position: Int) {
        if (currentLinePosition != position) {
            val oldPosition = currentLinePosition
            currentLinePosition = position
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            notifyItemChanged(position)
        }
    }

    // 歌词数据对比
    private class LyricDiffCallback : DiffUtil.ItemCallback<LyricLine>() {
        override fun areItemsTheSame(oldItem: LyricLine, newItem: LyricLine): Boolean {
            return oldItem.timeMs == newItem.timeMs
        }

        override fun areContentsTheSame(oldItem: LyricLine, newItem: LyricLine): Boolean {
            return oldItem == newItem
        }
    }
}

@Route(path = "/song/SongWord")
class SongWord : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: String = ""

    @JvmField
    @Autowired
    var currentPos: Int = 0  // 当前播放进度（毫秒）

    @JvmField
    @Autowired
    var currentSongDuration: Int = 0

    private lateinit var binding: ActivitySongwordBinding
    private lateinit var songWordViewModel: SongWordViewModel
    private lateinit var lyricAdapter: LyricAdapter
    private var lyricLines = listOf<LyricLine>()
    private var currentPlayTime = currentPos.toLong()
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongwordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        songWordViewModel = ViewModelProvider(this)[SongWordViewModel::class.java]
        initLyricRecyclerView()
        TheRouter.inject(this)

        // 初始化当前播放时间
        currentPlayTime = currentPos.toLong()

        if (id.isBlank()) {
            Toast.makeText(this, "歌曲ID不能为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.mdSongback.setOnClickListener { finish() }
        fetchSongWordData()
    }

    // 注册EventBus
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    // 解注册EventBus
    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun initLyricRecyclerView() {
        lyricAdapter = LyricAdapter()
        binding.lyricRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SongWord)
            adapter = lyricAdapter
            setHasFixedSize(true)
            itemAnimator = null  // 关闭动画，避免延迟
        }
    }

    // 接收播放器发送的进度事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayProgressEvent(event: PlayProgressEvent) {
        currentPlayTime = event.position.toLong()
        updateLyricPosition()

    }

    //接收播放器发送的切换歌曲事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChangeEvent(event: SongChangeEvent) {
        overridePendingTransition(0, R.anim.back_anim) // 第二个参数为退出动画
        finish()
    }

    // 接收播放器发送的强制关闭歌词事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseLyricEvent(event: CloseLyricEvent) {
        Log.d("LyricClose", "收到强制关闭事件，返回播放页")
        finish()
    }

    private fun fetchSongWordData() {
        songWordViewModel.fetchSongWordData(id.toLong())
        songWordViewModel.songWordData.observe(this) { songWordData ->
            songWordData?.let {
                parseLyrics(it)
                updateLyricPosition()  //解析歌词并且更新ui
            }
        }

        songWordViewModel.loadingState.observe(this) { state ->
            when (state) {
                is SongWordViewModel.LoadingState.Loading -> binding.lyricRecyclerView.visibility =
                    View.GONE

                is SongWordViewModel.LoadingState.Success -> binding.lyricRecyclerView.visibility =
                    View.VISIBLE

                is SongWordViewModel.LoadingState.Error -> {
                    Log.d("LyricDebug", "加载错误: ${state.message}")
                    binding.lyricRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    //解析歌词数据
    private fun parseLyrics(songWordData: SongWordData) {
        val rawLyric = songWordData.lrc.lyric
        Log.d("LyricDebug", "原始歌词: $rawLyric")

        if (rawLyric.isBlank()) {
            lyricLines = emptyList()
            lyricAdapter.submitList(lyricLines)
            return
        }  //没有歌词的情况

        val lyricList = mutableListOf<LyricLine>()
        val lines = rawLyric.split("\n")   //按行分割歌词
        val timeTagPattern = Regex("^\\[\\d{2}:\\d{2}\\.\\d{1,3}\\]\\s*")  //匹配时间的正则表达式

        for (line in lines) {  //行行解析
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue

            val timeTagMatch = timeTagPattern.find(trimmedLine)
            if (timeTagMatch != null) {
                val timeMs = LyricTimeParser.parseTimeTag(timeTagMatch.value)
                val content = trimmedLine.replace(timeTagPattern, "").trim()
                if (content.isNotBlank()) {
                    lyricList.add(LyricLine(timeMs, content))
                    Log.d("LyricDebug", "解析到歌词: $content (时间: $timeMs)")
                }
            }
        }

        lyricLines = lyricList.sortedBy { it.timeMs }  //按时间排序歌词的行
        Log.d("LyricDebug", "解析完成，共${lyricLines.size}行歌词")
        lyricAdapter.submitList(lyricLines)
    }

    private fun updateLyricPosition() {
        if (lyricLines.isEmpty()) return

        // 找到最后一句时间戳<= 当前播放时间的歌词
        var currentIndex = -1
        for (i in lyricLines.indices.reversed()) {
            if (lyricLines[i].timeMs <= currentPlayTime) {
                currentIndex = i
                break
            }
        }

        // 只有当索引变化时才更新，避免无效刷新
        if (currentIndex != -1) {
            lyricAdapter.updateCurrentLine(currentIndex)

            // 滚动到当前行（保持在屏幕中间）
            val layoutManager = binding.lyricRecyclerView.layoutManager as LinearLayoutManager
            layoutManager.scrollToPositionWithOffset(
                currentIndex,
                binding.lyricRecyclerView.height / 2 - dpToPx(30)  //调整，让歌词在水平中间
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
    