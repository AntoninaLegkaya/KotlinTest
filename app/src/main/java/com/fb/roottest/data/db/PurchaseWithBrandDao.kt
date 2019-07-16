package com.fb.roottest.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PurchaseWithBrandDao {

    @Transaction
    suspend fun getAllPurchasesWithBrands(): LiveData<List<PurchaseWithBrand>> {
        val purchaseWithBrandSource = MediatorLiveData<List<PurchaseWithBrand>>()
        val purchasesWithBrandEntity = withContext(Dispatchers.IO) {
            getPurchasesWithBrand()
        }
        withContext(Dispatchers.Main) {
            purchaseWithBrandSource.addSource(purchasesWithBrandEntity) {
                purchaseWithBrandSource.postValue(it)

            }
        }
        return purchaseWithBrandSource
    }

    @Query("SELECT * FROM purchases")
    fun getPurchasesWithBrand(): LiveData<List<PurchaseWithBrand>>
}