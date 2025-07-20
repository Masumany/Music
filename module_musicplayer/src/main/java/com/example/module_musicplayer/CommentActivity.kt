package com.example.module_musicplayer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_musicplayer.databinding.FragmentCommentBinding
import com.example.module_musicplayer.viewmodel.CommentViewModel
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route

@Route(path = "/module_musicplayer/commentActivity")
class CommentActivity : AppCompatActivity() {

    @JvmField
    @Autowired
    var id: String? = null

    private lateinit var binding: FragmentCommentBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentViewModel: CommentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        TheRouter.inject(this)
        Log.d("接收参数", "id: $id")
        super.onCreate(savedInstanceState)
        binding = FragmentCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        commentViewModel = ViewModelProvider(this)[CommentViewModel::class.java]
        initRecyclerView()
        binding.ivBack.setOnClickListener { finish() }
        loadCommentData()
        observeCommentData()
    }

    private fun initRecyclerView() {
        // 初始化时创建一次适配器
        commentAdapter = CommentAdapter(emptyList())
        binding.rvComment.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            adapter = commentAdapter  // 设置给RecyclerView
        }
    }

    private fun loadCommentData() {
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "歌曲ID为空", Toast.LENGTH_SHORT).show()
            return
        }
        commentViewModel.fetchCommentData(id!!)
    }

    private fun observeCommentData() {
        commentViewModel.hotComments.observe(this) { hotComments ->
            Log.d("CommentData", "评论数量: ${hotComments.size}")  // 调试用，确认数据不为空

            if (hotComments.isNotEmpty()) {
                commentAdapter.updateComments(hotComments)
            } else {
                Toast.makeText(this, "暂无评论", Toast.LENGTH_SHORT).show()
            }
        }

        commentViewModel.errorMsg.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}