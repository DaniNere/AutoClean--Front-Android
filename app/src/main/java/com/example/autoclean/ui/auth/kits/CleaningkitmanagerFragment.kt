package com.example.autoclean.ui.auth.kits

import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentCleaningkitmanagerBinding
import com.example.autoclean.ui.adapter.CleaningPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CleaningkitmanagerFragment : Fragment() {

    private var _binding: FragmentCleaningkitmanagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleaningkitmanagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTabLayout()
    }

    private fun initTabLayout() {
        val pageAdapter = CleaningPagerAdapter(requireActivity())
        binding.viewPager.adapter = pageAdapter

        pageAdapter.addFragment(CleaningKitFragment(), R.string.cleaning_kit_title)
        pageAdapter.addFragment(KitDetailsFragment(), R.string.kit_details_title)
        pageAdapter.addFragment(ChooseKitFragment(), R.string.choose_kit_title)

        binding.viewPager.offscreenPageLimit = pageAdapter.itemCount

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = null
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {

                    val width = if (it.position == 0) {
                        resources.getDimensionPixelSize(R.dimen.tab_indicator_width_large)
                    } else {
                        resources.getDimensionPixelSize(R.dimen.tab_indicator_width_small)
                    }


                    val indicator = ShapeDrawable()
                    indicator.paint.color = requireContext().getColor(R.color.secondary_variant2)
                    indicator.intrinsicHeight = resources.getDimensionPixelSize(R.dimen.tab_indicator_height)
                    indicator.intrinsicWidth = width


                    binding.tabLayout.setSelectedTabIndicator(indicator)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
