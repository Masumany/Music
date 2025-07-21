package com.example.module_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.module_details.databinding.FragmentSingermvBinding
import kotlinx.coroutines.launch
import viewmodel.SingerMvViewModel

class SingerMvFragment:Fragment (){
    private lateinit var binding: FragmentSingermvBinding
    private lateinit var singerMvViewModel: SingerMvViewModel
    private lateinit var singerMvAdapter: SingMvAdapter
    private var singerId: Long = 0
    private lateinit var singerMvList: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
    : View {
        binding = FragmentSingermvBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        singerMvViewModel= ViewModelProvider(this).get(SingerMvViewModel::class.java)

        singerMvAdapter= SingMvAdapter(emptyList())

        singerMvList=binding.singerMvRv
        singerMvList.adapter=singerMvAdapter
        singerMvList.layoutManager= LinearLayoutManager(requireContext())

        if(singerId!=0L){
            lifecycleScope.launch {
                fetchMv(singerId)
            }
        }


        observeMv()
    }

    private suspend fun fetchMv(singerId:Long) {
        if(singerId==null ||singerId<=0){
            Log.d("SingerMvFragment", "Invalid singerId: $singerId")
            return
        }
        try {
            singerMvViewModel.getSingerMvData(singerId)
        }catch (e:Exception){}
        Log.d("SingerMvFragment", "Observing MV data")
    }

    private fun observeMv() {
        singerMvViewModel.singerMvData.observe(viewLifecycleOwner){response->
            if (response!=null && response.code==200){
                singerMvList.adapter=SingMvAdapter(response.mvs)
            }else{
                Log.d("SingerMvActivity", "observeMv: ${response.mvs} ")
            }
        }

    }

    fun setSingerId(id: Long) {
        if (id <= 0) {
            Log.e("SingerSong", "setSingerId: 无效ID=$id（必须>0）")
            return
        }
        this.singerId = id
        Log.d("SingerSong", "setSingerId: 成功接收ID=$id")

        // 如果Fragment已初始化完成，立即请求数据
        if (isAdded && ::singerMvViewModel.isInitialized) {
            Log.d("SingerSong", "ID已接收且Fragment就绪，立即请求数据")
            lifecycleScope.launch { fetchMv(id) }
        } else {
            Log.d("SingerSong", "ID已接收，等待Fragment初始化完成后请求")
        }
    }



}