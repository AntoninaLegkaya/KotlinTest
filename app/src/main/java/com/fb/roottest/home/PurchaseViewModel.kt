package com.fb.roottest.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.fb.roottest.data.db.Purchase

class PurchaseViewModel:ViewModel() {
    val name = ObservableField<String>()
    val cost = ObservableField<String>()
    val count = ObservableField<String>()


    fun start(purchase: Purchase) {
        name.set(purchase.purchase)
        cost.set(purchase.cost.toString())
        count.set(purchase.count.toString())
    }
}