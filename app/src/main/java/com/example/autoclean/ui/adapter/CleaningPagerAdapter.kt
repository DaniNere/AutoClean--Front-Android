package com.example.autoclean.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class CleaningPagerAdapter (fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

    private val fragmentList: MutableList<Fragment> = ArrayList()


    fun addFragment(fragment: Fragment, title: Int) {
        fragmentList.add(fragment)

    }
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

}