package com.fb.roottest.home

import android.os.Bundle
import android.view.View
import com.fb.roottest.MainActivity
import com.fb.roottest.R
import com.fb.roottest.base.BaseFragment
import com.fb.roottest.base.paralax.BottomSheetBehaviorGoogleMapsLike
import com.fb.roottest.base.paralax.ItemPagerAdapter
import com.fb.roottest.base.paralax.MergedAppBarBehavior
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HomeFragment : BaseFragment<FragmentHomeBinding>(), PurchaseClickListener {

//    private var viewModel: HomeViewModel

    override val contentLayoutId: Int
        get() = R.layout.fragment_home

    var mDrawables = intArrayOf(
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3,
        R.drawable.cheese_3
    )
    private val callback = BottomSheetCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setupBinding(binding: FragmentHomeBinding) {
        val toolBar = binding.toolbar
        val viewModel = obtainViewModel(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.listener=this
        binding.inputSheet.listener = this
        binding.inputSheet.viewModel=viewModel
        viewModel.start()

        (activity as MainActivity).setSupportActionBar(toolBar)

        val actionBar = (activity as MainActivity).getSupportActionBar()
        actionBar?.let {
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

    override fun onNamePurchaseTextChanged(text: String) {
        binding.viewModel?.onNameChanged(text)
    }

    override fun onCostPurchaseTextChanged(text: String) {
        binding.viewModel?.onCostChanged(text)
    }
    override fun onCountPurchaseTextChanged(text: String) {
        binding.viewModel?.onCountChanged(text)
    }


    override fun insertPurchase() {
       val purchase= binding.inputSheet.namePurchaseEditText.text.toString()
       val cost=  binding.inputSheet.costPurchaseEditText.text.toString()
       val count=  binding.inputSheet.countPurchaseEditText.text.toString()
        binding.viewModel?.insertPurchase(Purchase(purchase.toString(), cost.toInt(), count.toInt()))
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