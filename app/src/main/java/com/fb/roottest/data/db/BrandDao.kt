package com.fb.roottest.data.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Dao
interface BrandDao {

    @Transaction
    suspend fun insertBrandTransaction(brand: Brand): LiveData<Long> {
        val brandSource = MediatorLiveData<Long>()
        val brandEntity = MediatorLiveData<Long>()
        val idInsert = insertBrand(brand)

        Log.d("devcpp", "BrandDao insert Brand ID:  " + idInsert + " NAME: " + brand.brand)

        withContext(Dispatchers.Main) {
            brandEntity.value = idInsert
            brandSource.addSource(brandEntity) {
                brandSource.postValue(it)

            }
        }
        return brandSource
    }

    @Transaction
    suspend fun getBrandByIdTransaction(id: Long): LiveData<Brand> {
        val brandSource = MediatorLiveData<Brand>()

        withContext(Dispatchers.Main) {
            brandSource.addSource(getBrandById(id)) {
                brandSource.postValue(it)
            }

        }
        return brandSource
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBrand(brand: Brand): Long

    @Query("DELETE FROM brands")
    fun clearBrand()

    @Query("SELECT * FROM brands ORDER BY brand ASC")
    fun getAllBrandsEntity(): LiveData<List<Brand>>

    @Query("SELECT * FROM brands WHERE brand_id == :id")
    fun getBrandById(id: Long): LiveData<Brand>
}