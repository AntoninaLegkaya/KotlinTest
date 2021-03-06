package com.fb.roottest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fb.roottest.BuildConfig

@Database(entities = [Purchase::class], version = BuildConfig.VERSION_CODE, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
}