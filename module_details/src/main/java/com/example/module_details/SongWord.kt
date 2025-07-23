package com.example.module_details

import PlayProgressEvent
import SongChangeEvent
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
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
    val timeMs: Long,  // 演唱开始时间戳（毫秒）
    val content: String  // 歌词内容
)

// 时间解析工具类
object LyricTimeParser {
    fun parseTimeTag(timeTag: String): Long {
        return try {
            val timeStr = timeTag.replace(Regex("\\s+"), "").removeSurrounding("[", "]")
            val minSec = timeStr.split(":")
            if (minSec.size < 2) return 0L

            val minutes = minSec[0].toLongOrNull() ?: 0L
            val secMs = minSec[1].split(".")
            if (secMs.isEmpty()) return 0L

            val seconds = secMs[0].toLongOrNull() ?: 0L
            val milliseconds = if (secMs.size > 1) {
                secMs[1].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
            } else {
                0L
            }

            minutes * 60 * 1000 + seconds * 1000 + milliseconds
        } catch (e: Exception) {
            Log.e("TimeParseError", "解析失败: $timeTag", e)
            0L
        }
    }
}

// 歌词适配器
class LyricAdapter : ListAdapter<LyricLine, LyricAdapter.LyricViewHolder>(LyricDiffCallback()) {

    private var currentLinePosition = -1  // 当前行高亮

    inner class LyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lyricText: TextView = itemView.findViewById(R.id.tv_lyric_content)

        fun bind(lyricLine: LyricLine, isCurrentLine: Boolean) {
            lyricText.text = lyricLine.content

            if (isCurrentLine) {
                lyricText.setTextColor(itemView.context.getColor(R.color.white))
                lyricText.textSize = 18f
                lyricText.alpha = 1.0f
            } else {
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

    fun clearCurrentLine() {
        val oldPosition = currentLinePosition
        currentLinePosition = -1
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
    }

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
    private var lastUpdatedLine = -1
    private val SCROLL_DELAY = 100L

    // 新增状态追踪变量
    private var isLyricLoaded = false  // 歌词是否加载完成
    private var initialSyncDone = false  // 初始同步是否完成
    private var pendingPosition = -1  // 等待歌词加载的进度位置

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongwordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化ViewModel和适配器
        songWordViewModel = ViewModelProvider(this)[SongWordViewModel::class.java]
        initLyricRecyclerView()
        TheRouter.inject(this)

        // 初始化当前播放时间
        currentPlayTime = currentPos.toLong()
        Log.d("LyricDebug", "初始化 - 歌曲ID: $id, 初始进度: $currentPlayTime")

        // 处理系统栏
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 验证歌曲ID
        if (id.isBlank()) {
            Toast.makeText(this, "歌曲ID不能为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.mdSongback.setOnClickListener { finish() }
        fetchSongWordData()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LyricDebug", "页面可见 - 触发同步")
        // 页面可见时强制同步一次
        if (isLyricLoaded) {
            updateLyricPosition()
        } else if (pendingPosition != -1) {
            // 如果有等待的进度，用它来同步
            currentPlayTime = pendingPosition.toLong()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d("LyricDebug", "EventBus注册")
        }
        startProgressChecker()
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
            Log.d("LyricDebug", "EventBus解注册")
        }
        handler.removeCallbacksAndMessages(null)
    }

    private fun initLyricRecyclerView() {
        lyricAdapter = LyricAdapter()
        binding.lyricRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SongWord)
            adapter = lyricAdapter
            setHasFixedSize(true)
            itemAnimator = null
            isNestedScrollingEnabled = false
            setItemViewCacheSize(20)
        }
    }

    private fun startProgressChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isLyricLoaded) {
                    updateLyricPosition()
                }
                handler.postDelayed(this, SCROLL_DELAY)
            }
        }, SCROLL_DELAY)
    }

    // 接收进度事件 - 重点修复
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayProgressEvent(event: PlayProgressEvent) {
        // 严格验证歌曲ID匹配
        if (event.songId != id) {
            Log.d("LyricDebug", "忽略事件 - 歌曲ID不匹配: ${event.songId} vs $id")
            return
        }

        // 无论歌词是否加载完成，都更新当前播放时间
        currentPlayTime = event.position.toLong()
        Log.d("LyricDebug", "收到进度事件 - 位置: ${event.position}, 歌词加载状态: $isLyricLoaded")

        // 如果歌词未加载完成，保存进度等待加载
        if (!isLyricLoaded) {
            pendingPosition = event.position
            return
        }

        // 歌词已加载，立即更新位置
        updateLyricPosition()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSongChangeEvent(event: SongChangeEvent) {
        Log.d("LyricDebug", "收到歌曲切换事件 - 新ID: ${event.newSongId}, 当前ID: $id")
        if (event.newSongId != id) {
            // 重置所有状态
            id = event.newSongId
            currentPlayTime = event.initialPosition.toLong()
            pendingPosition = event.initialPosition
            isLyricLoaded = false
            initialSyncDone = false
            lyricLines = emptyList()
            lyricAdapter.clearCurrentLine()
            lyricAdapter.submitList(lyricLines)

            // 加载新歌词
            fetchSongWordData()
        }
    }

    private fun fetchSongWordData() {
        Log.d("LyricDebug", "开始加载歌词 - 歌曲ID: $id")
        songWordViewModel.fetchSongWordData(id.toLong())
        songWordViewModel.songWordData.observe(this) { songWordData ->
            songWordData?.let {
                parseLyrics(it)
                isLyricLoaded = true
                Log.d("LyricDebug", "歌词加载完成 - 共${lyricLines.size}行")

                // 歌词加载后立即同步位置
                if (pendingPosition != -1) {
                    currentPlayTime = pendingPosition.toLong()
                    pendingPosition = -1
                }

                // 强制初始同步
                forceInitialSync()
            }
        }

        songWordViewModel.loadingState.observe(this) { state ->
            when (state) {
                is SongWordViewModel.LoadingState.Loading -> {
                    binding.lyricRecyclerView.visibility = View.GONE
                    Log.d("LyricDebug", "正在加载歌词...")
                }
                is SongWordViewModel.LoadingState.Success -> {
                    binding.lyricRecyclerView.visibility = View.VISIBLE
                }
                is SongWordViewModel.LoadingState.Error -> {
                    Log.e("LyricDebug", "加载错误: ${state.message}")
                    binding.lyricRecyclerView.visibility = View.GONE
                    // 显示错误信息但保持事件监听
                    binding.lyricRecyclerView.visibility = View.VISIBLE
                    lyricLines = listOf(LyricLine(0, "歌词加载失败: ${state.message}"))
                    lyricAdapter.submitList(lyricLines)
                    isLyricLoaded = true
                }
            }
        }
    }

    // 强制初始同步逻辑 - 解决首页进入不同步问题
    private fun forceInitialSync() {
        if (initialSyncDone || lyricLines.isEmpty()) return

        Log.d("LyricDebug", "执行强制初始同步 - 位置: $currentPlayTime")
        // 手动触发三次更新，确保 RecyclerView已布局完成
        val syncRunnable = object : Runnable {
            var attempts = 0
            override fun run() {
                if (attempts < 3) {
                    updateLyricPosition()
                    attempts++
                    handler.postDelayed(this, 200)
                } else {
                    initialSyncDone = true
                }
            }
        }
        handler.post(syncRunnable)
    }

    private fun parseLyrics(songWordData: SongWordData) {
        val rawLyric = songWordData.lrc.lyric
        Log.d("LyricDebug", "解析歌词: ${rawLyric.take(100)}...")

        if (rawLyric.isBlank()) {
            lyricLines = listOf(LyricLine(0, "暂无歌词"))
            lyricAdapter.submitList(lyricLines)
            return
        }

        val lyricList = mutableListOf<LyricLine>()
        val lines = rawLyric.split("\n")
        val timeTagPattern = Regex("^\\[\\d{2}:\\d{2}\\.\\d{1,3}\\]\\s*")

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue

            val timeTagMatch = timeTagPattern.find(trimmedLine)
            if (timeTagMatch != null) {
                val timeMs = LyricTimeParser.parseTimeTag(timeTagMatch.value)
                val content = trimmedLine.replace(timeTagPattern, "").trim()
                if (content.isNotBlank()) {
                    lyricList.add(LyricLine(timeMs, content))
                }
            }
        }

        lyricLines = if (lyricList.isNotEmpty()) {
            lyricList.sortedBy { it.timeMs }
        } else {
            listOf(LyricLine(0, "暂无有效歌词"))
        }

        lyricAdapter.submitList(lyricLines)
    }

    private fun updateLyricPosition() {
        if (lyricLines.isEmpty()) return

        // 查找当前应该显示的歌词行
        var currentIndex = -1
        for (i in lyricLines.indices.reversed()) {
            // 扩大容错范围到300ms，解决时间差问题
            if (lyricLines[i].timeMs <= currentPlayTime + 300) {
                currentIndex = i
                break
            }
        }

        // 处理歌曲结束情况
        if (currentPlayTime >= currentSongDuration - 1000) {
            currentIndex = lyricLines.lastIndex
        }

        // 处理未找到匹配行的情况
        if (currentIndex == -1 && lyricLines.isNotEmpty()) {
            currentIndex = 0
        }

        // 更新当前行并滚动
        if (currentIndex != -1 && currentIndex != lastUpdatedLine) {
            lyricAdapter.updateCurrentLine(currentIndex)
            lastUpdatedLine = currentIndex

            // 确保在UI线程执行滚动
            binding.lyricRecyclerView.post {
                val layoutManager = binding.lyricRecyclerView.layoutManager as LinearLayoutManager
                // 计算居中位置
                val targetY = binding.lyricRecyclerView.height / 2 - dpToPx(30)
                layoutManager.scrollToPositionWithOffset(currentIndex, targetY)
                Log.d("LyricDebug", "滚动到行: $currentIndex, 位置: $targetY")
            }
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
    