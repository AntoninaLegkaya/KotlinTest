package com.fb.roottest.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.fb.roottest.MainActivity
import com.fb.roottest.R
import com.fb.roottest.base.BaseFragment
import com.fb.roottest.base.paralax.BottomSheetBehaviorGoogleMapsLike
import com.fb.roottest.base.paralax.MergedAppBarBehavior
import com.fb.roottest.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.viewpager.widget.ViewPager
import com.fb.roottest.base.paralax.ItemPagerAdapter

class HomeFragment : Fragment() {
    var mDrawables = intArrayOf(
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3
    )
    private val callback = BottomSheetCallback()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater,R.layout.fragment_home,container,  false)
        setupBinding(binding)
        binding.setLifecycleOwner(this)
        return binding.root
    }

     fun setupBinding(binding: FragmentHomeBinding) {
        val toolBar = binding.toolbar
        (activity as MainActivity).setSupportActionBar(toolBar)

        val actionBar =  (activity as MainActivity).getSupportActionBar()
        actionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle("Default Title")
        }
        val behavior = BottomSheetBehaviorGoogleMapsLike.from(binding.bottomSheet)
        behavior.addBottomSheetCallback(callback)
        val mergedAppBarBehavior = MergedAppBarBehavior.from(binding.mergedappbarlayout)
        mergedAppBarBehavior.setToolbarTitle("Merged Toolbar")
        mergedAppBarBehavior.setNavigationOnClickListener(View.OnClickListener {
            behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT
        })
        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT

         val adapter = ItemPagerAdapter(binding.root.context, mDrawables)
         val viewPager = binding.pager
         viewPager.adapter = adapter
    }

    class BottomSheetCallback() :
        BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Nothing impl
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                }
            }
        }
    }
}