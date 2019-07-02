package com.fb.roottest.base.paralax

import android.content.Context
import android.widget.LinearLayout
import android.view.ViewGroup
import android.content.Context.LAYOUT_INFLATER_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.fb.roottest.R
import androidx.core.content.ContextCompat.getSystemService




class ItemPagerAdapter(internal var mContext: Context, internal val mItems: IntArray) : PagerAdapter() {
    internal var mLayoutInflater: LayoutInflater

    init {
        this.mLayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
    }

    override fun getCount(): Int {
        return mItems.size
    }

   override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false)
        val imageView = itemView.findViewById(R.id.imageView) as ImageView
        imageView.setImageResource(mItems[position])
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}