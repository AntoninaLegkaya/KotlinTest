package com.fb.roottest.data.repository

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.AppDataBase
import com.fb.roottest.data.db.Brand
import com.fb.roottest.data.db.Purchase
import com.fb.roottest.data.db.PurchaseWithBrand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RootRepository(val appDataBase: AppDataBase) : Repository, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    companion object {
        @Volatile
        private var INSTANSE: RootRepository? = null

        fun getInstance(appDataBase: AppDataBase): RootRepository = INSTANSE ?: synchronized(this) {
            INSTANSE ?: init(appDataBase).also { INSTANSE = it }
        }

        private fun init(appDataBase: AppDataBase) = RootRepository(appDataBase)
    }

    override fun getAllPurchase(): MediatorLiveData<ResultQuery<List<Purchase>>> {
        val result = MediatorLiveData<ResultQuery<List<Purchase>>>()
        launch(Dispatchers.IO) {
            val dbData = appDataBase.purchaseDao().getAllPurchasesEntity()
            withContext(Dispatchers.Main) {
                result.addSource(dbData) {
                    result.postValue(ResultQuery.SuccessResult(it))
                }
            }
        }
        return result
    }

    override fun insertPurchase(purchase: Purchase) {
        launch(Dispatchers.IO) {
            appDataBase.purchaseDao().insertPurchaseTransaction(purchase)
        }
    }

    override fun insertBrand(brand: Brand):MediatorLiveData<ResultQuery<Long>> {
        val result: MediatorLiveData<ResultQuery<Long>> = MediatorLiveData()
        launch(Dispatchers.IO) {
           val insertId=  appDataBase.brandDao().insertBrandTransaction(brand)
            withContext(Dispatchers.Main) {
                result.addSource(insertId) {
                    result.postValue(ResultQuery.SuccessResult(it))
                }
            }
        }
        return result
    }

    override fun insertNewBrand(brand: Brand) {
        launch(Dispatchers.IO) {
            appDataBase.brandDao().insertBrandTransaction(brand)
        }
    }

    override fun getBrandById(id: Long): MediatorLiveData<ResultQuery<Brand>> {
       val result: MediatorLiveData<ResultQuery<Brand>> = MediatorLiveData()
        launch(Dispatchers.IO) {
            val brand= appDataBase.brandDao().getBrandByIdTransaction(id)
            withContext(Dispatchers.Main) {
                result.addSource(brand){
                   result.postValue(ResultQuery.SuccessResult(it))
                }
            }
        }
        return result
    }

    override fun getAllBrands(): MediatorLiveData<ResultQuery<List<Brand>>> {
        val result = MediatorLiveData<ResultQuery<List<Brand>>>()
        launch(Dispatchers.IO) {
            val dbData = appDataBase.brandDao().getAllBrandsEntity()
            withContext(Dispatchers.Main) {
                result.addSource(dbData) {
                    result.postValue(ResultQuery.SuccessResult(it))
                }
            }
        }
        return result
    }

    override fun closeDataBase() {
        appDataBase.close()
    }

}
