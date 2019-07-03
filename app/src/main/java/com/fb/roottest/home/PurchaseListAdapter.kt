package com.fb.roottest.home

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fb.roottest.R
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.databinding.ItemPurchaseBinding
import com.fb.roottest.databinding.ItemTitlesPurchaseBinding
import com.fb.roottest.util.PurchaseDiffUtils
import com.fb.roottest.util.inflateView

const val TITLE_ITEM_TYPE = 0
const val DEFAULT_ITEM_TYPE = 1

class PurchaseListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TITLE_ITEM_TYPE -> {
            TitlePurchaseItemViewHolder(parent.inflateView(R.layout.item_titles_purchase))
        }
        else -> {
            PurchaseListItemViewHolder(parent.inflateView(R.layout.item_purchase))
        }
    }

    private var itemsList: MutableList<Purchase> = emptyList<Purchase>().toMutableList()

    override fun getItemCount(): Int {
        return itemsList.size+1
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> TITLE_ITEM_TYPE
        else -> DEFAULT_ITEM_TYPE
    }

    fun setData(purchaseList: MutableList<Purchase>) {
        val diffCallback = PurchaseDiffUtils(getData(), purchaseList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        (getData() as? MutableList)?.run {
            clear()
            addAll(purchaseList)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    fun getData(): MutableList<Purchase> {
        return itemsList
    }

    fun setItems(itemsList: MutableList<Purchase>) {
        this.itemsList = itemsList.apply { setData(this) }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > 0 && holder is PurchaseListItemViewHolder) {
            holder.bind(itemsList[position-1])
        }
    }

    inner class PurchaseListItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemPurchaseBinding>(itemView)

        fun bind(item: Purchase) {
            binding?.viewModel = PurchaseViewModel()
            binding?.viewModel?.start(item)
        }

    }

    inner class TitlePurchaseItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemTitlesPurchaseBinding>(itemView)
    }
}


