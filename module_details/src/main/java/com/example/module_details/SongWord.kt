package com.example.module_details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.module_details.databinding.ActivitySongwordBinding
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import data.SongWordData
import viewmodel.SongWordViewModel

@Route(path = "/song/SongWord")
class SongWord : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: String = ""  // 通过路由接收歌曲ID

    private lateinit var binding: ActivitySongwordBinding
    private lateinit var songWordViewModel: SongWordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongwordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化ViewModel
        songWordViewModel = ViewModelProvider(this)[SongWordViewModel::class.java]

        // 注入路由参数
        TheRouter.inject(this)

        // 适配系统状态栏
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 验证ID参数
        if (id.isBlank()) {
            Toast.makeText(this, "歌曲ID不能为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 返回按钮点击事件
        binding.mdSongback.setOnClickListener {
            finish()
        }

        // 初始隐藏歌词区域
        binding.mdWord.visibility = View.GONE

        // 获取歌词数据
        fetchSongWordData()
    }

    private fun fetchSongWordData() {
        // 调用ViewModel获取歌词（使用优化后的fetch方法）
        songWordViewModel.fetchSongWordData(id.toLong())

        // 观察歌词数据变化
        songWordViewModel.songWordData.observe(this) { songWordData ->
            songWordData?.let { data ->
                // 处理歌词：去除时间戳并清理空行
                val rawLyric = data.lrc.lyric
                // 正则替换时间戳（[mm:ss.SSS]格式）
                val cleanedLyric = rawLyric.replace(Regex("\\[\\d+:\\d+\\.\\d+\\]"), "").trim()
                // 合并连续空行为单个换行
                val finalLyric = cleanedLyric.replace(Regex("\n{2,}"), "\n")
                // 显示处理后的歌词
                binding.mdWord.text = if (finalLyric.isBlank()) "暂无歌词" else finalLyric
            }
        }

        // 观察加载状态变化
        songWordViewModel.loadingState.observe(this) { state ->
            when (state) {
                is SongWordViewModel.LoadingState.Loading -> {
                    binding.mdWord.visibility = View.GONE
                }
                is SongWordViewModel.LoadingState.Success -> {
                    binding.mdWord.visibility = View.VISIBLE
                }
                is SongWordViewModel.LoadingState.Error -> {
                    binding.mdWord.visibility = View.VISIBLE
                    binding.mdWord.text = state.message
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}