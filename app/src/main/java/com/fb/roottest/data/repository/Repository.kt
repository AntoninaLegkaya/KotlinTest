package com.fb.roottest.data.repository

import androidx.lifecycle.MediatorLiveData
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.Brand
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.db.PurchaseWithBrand

interface Repository {
    fun getAllPurchase():MediatorLiveData<ResultQuery<List<Purchase>>>
    fun insertPurchase(purchase: Purchase)
    fun insertBrand(brand: Brand):MediatorLiveData<ResultQuery<Long>>
    fun insertNewBrand(brand: Brand)
    fun getBrandById(id: Long):MediatorLiveData<ResultQuery<Brand>>
    fun getAllBrands():MediatorLiveData<ResultQuery<List<Brand>>>
}