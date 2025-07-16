package com.example.module_musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.module_musicplayer.databinding.FragmentCommentBinding

class CommentFragment : Fragment() {

    // 使用ViewBinding替代findViewById
    private lateinit var binding: FragmentCommentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 初始化ViewBinding
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 通过binding访问控件，无需findViewById
        binding.mpImageView.setOnClickListener {
            // 安全地获取Activity实例
            (activity as? MusicPlayerActivity)?.hideFragment()
        }

        // 可以直接访问文本控件（示例）
        // binding.mpText.text = "评论区内容"
    }
}
