package com.fb.roottest.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Dao
interface PurchaseDao {

    @Transaction
    open suspend fun insertPurchase(purchase: Purchase) {
        insertPurchaseEntity(purchase)
    }

    @Transaction
    open suspend fun getAllPurchases(): LiveData<List<Purchase>> {
        val purchaseSource = MediatorLiveData<List<Purchase>>()
        val purchasesEntity = withContext(Dispatchers.IO) {
            getAllPurchasesEntity()
        }
        withContext(Dispatchers.Main) {
            purchaseSource.addSource(purchasesEntity) {
                purchaseSource.postValue(it)

            }
        }
        return purchaseSource
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPurchaseEntity(purchase: Purchase)

    @Query("DELETE FROM purchases")
    fun clearPurchases()

    @Query("SELECT * FROM purchases ORDER BY purchase ASC")
    fun getAllPurchasesEntity(): LiveData<List<Purchase>>

}