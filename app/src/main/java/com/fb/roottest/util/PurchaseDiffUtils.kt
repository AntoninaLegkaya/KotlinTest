package com.fb.roottest.util

import androidx.recyclerview.widget.DiffUtil
import com.fb.roottest.data.db.Purchase

class PurchaseDiffUtils (private val oldPurchase: MutableList<Purchase>, private val newPurchase: MutableList<Purchase>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldPurchase = oldPurchase[oldItemPosition]
        val newPurchase = newPurchase[newItemPosition]

        return (oldPurchase.purchase == newPurchase.purchase)
    }

    override fun getOldListSize(): Int = oldPurchase.size

    override fun getNewListSize(): Int = newPurchase.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }
}