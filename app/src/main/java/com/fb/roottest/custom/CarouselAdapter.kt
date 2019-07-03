package com.fb.roottest.custom

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.fb.roottest.R
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.databinding.ItemCaruselPurchaseBinding
import com.fb.roottest.home.PurchaseViewModel
import com.fb.roottest.util.inflateView

class CarouselAdapter : PagerAdapter() {
    private var purchases: MutableList<Purchase> = emptyList<Purchase>().toMutableList()
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return purchases.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val itemView = container.inflateView(R.layout.item_carusel_purchase)
        val binding = DataBindingUtil.bind<ItemCaruselPurchaseBinding>(itemView)
        binding?.viewModel = PurchaseViewModel()
        binding?.viewModel?.start(purchases.get(position))
        container.addView(itemView)
        if (position == 0) {
            binding?.dragLeft?.visibility = View.GONE
        } else if (position == purchases.size - 1) {
            binding?.dragRight?.visibility = View.GONE
        }
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    fun getData(): MutableList<Purchase> {
        return purchases
    }

    fun setItems(itemsList: MutableList<Purchase>) {
        this.purchases = itemsList
        notifyDataSetChanged()
    }

}