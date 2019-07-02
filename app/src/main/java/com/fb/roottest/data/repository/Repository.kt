package com.fb.roottest.data.repository

import androidx.lifecycle.MediatorLiveData
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.Purchase

interface Repository {
    fun getAllPurchase():MediatorLiveData<ResultQuery<List<Purchase>>>
    fun insertPurchase(purchase: Purchase)
}