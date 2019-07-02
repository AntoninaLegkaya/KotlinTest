package com.fb.roottest.data.repository

import androidx.lifecycle.MediatorLiveData
import com.fb.roottest.data.ResultQuery
import com.fb.roottest.data.db.AppDataBase
import com.fb.roottest.data.db.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RootRepository private constructor(val appDataBase: AppDataBase) : Repository, CoroutineScope {
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

    override fun  insertPurchase(purchase: Purchase) {
       launch (Dispatchers.IO){
           appDataBase.purchaseDao().insertPurchase(purchase)
       }
    }
}
