package com.example.module_mvplayer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.module_mvplayer.adapter.CommentAdapter
import com.example.module_mvplayer.databinding.MvCommentSheetBinding
import com.example.module_mvplayer.viewModel.LoadState
import com.example.module_mvplayer.viewModel.MvCommentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheetDialogFragment(private val mvId: String): BottomSheetDialogFragment() {

    private var _binding: MvCommentSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MvCommentViewModel by viewModels()
    private lateinit var adapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MvCommentSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CommentAdapter()
        binding.rvComment.adapter = adapter
        binding.rvComment.layoutManager = LinearLayoutManager(requireContext())
        binding.refreshComment.setOnClickListener {
            loadCommentData()
        }

        loadCommentData()
    }

    private fun loadCommentData() {
        viewModel.loadComments(mvId)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.commentList.collect {
                adapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.loadState.collect {
                when (it) {
                    is LoadState.Init -> {
                        binding.loadingComment.visibility = View.VISIBLE
                        binding.errorComment.visibility = View.GONE
                    }
                    is LoadState.Success -> {
                        binding.loadingComment.visibility = View.GONE
                        binding.errorComment.visibility = View.GONE
                    }
                    is LoadState.Error -> {
                        binding.loadingComment.visibility = View.GONE
                        binding.errorComment.visibility = View.VISIBLE
                    }
                    is LoadState.Loading -> {
                        binding.loadingComment.visibility = View.VISIBLE
                        binding.errorComment.visibility = View.GONE
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}