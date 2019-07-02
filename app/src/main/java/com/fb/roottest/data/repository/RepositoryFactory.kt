package com.fb.roottest.data.repository

import android.content.Context
import androidx.room.Room
import com.fb.roottest.data.db.AppDataBase

class RepositoryFactory {
    companion object {
        fun provideRepository(context: Context): Repository {
            val appDataBase: AppDataBase =
                Room.databaseBuilder(context, AppDataBase::class.java, "database-app").fallbackToDestructiveMigration().build()
            return RootRepository.getInstance(appDataBase)
        }
    }
}