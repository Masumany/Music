package Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.module_hot.ui.fragment.HotFragment
import com.example.module_personage.ui.fragment.PersonageFragment
import com.example.module_recommened.ui.RecommendFragment

class Vp2Adapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        RecommendFragment(),
        HotFragment(),
        PersonageFragment()
    )

    override fun getItemCount(): Int = fragments.size


    override fun createFragment(position: Int): Fragment = fragments[position]

}