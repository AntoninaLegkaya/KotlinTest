package com.fb.roottest.home

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.fb.roottest.data.db.Purchase

class PurchaseViewModel:ViewModel() {
    val name = ObservableField<String>()
    val cost = ObservableField<String>()
    val count = ObservableField<String>()
    val brand = ObservableField<String>()
    val imageBase64 = ObservableField<String>()



    fun start(purchase: Purchase) {
        name.set(purchase.purchase)
        brand.set(purchase.brandName)
        cost.set(purchase.cost.toString())
        count.set(purchase.count.toString())
        imageBase64.set(purchase.image.toString())
    }
}