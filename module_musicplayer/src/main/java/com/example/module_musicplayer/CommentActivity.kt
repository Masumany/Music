package com.example.module_musicplayer

import Event.CloseCommentEvent
import Event.CloseLyricEvent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_musicplayer.Adapter.CommentAdapter
import com.example.module_musicplayer.databinding.FragmentCommentBinding
import com.example.module_musicplayer.viewmodel.CommentViewModel
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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

    //注册EventBus
    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered( this)){
            EventBus.getDefault().register(this)
        }
    }

    //解注册EventBus
    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().isRegistered( this)){
            EventBus.getDefault().unregister(this)
        }
    }

    private fun initRecyclerView() {
        commentAdapter = CommentAdapter(emptyList())
        binding.rvComment.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            adapter = commentAdapter
        }
    }

    private fun loadCommentData() {
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "歌曲ID为空", Toast.LENGTH_SHORT).show()
            return
        }
        commentViewModel.fetchCommentData(id!!)
    }

    //订阅事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun CloseCommentEvent(event: CloseCommentEvent) {
        Log.d("CommentClose", "收到强制关闭事件，返回播放页")
        finish()//处理事件
    }

    private fun observeCommentData() {
        commentViewModel.hotComments.observe(this) { hotComments ->
            Log.d("CommentData", "评论数量: ${hotComments.size}")

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