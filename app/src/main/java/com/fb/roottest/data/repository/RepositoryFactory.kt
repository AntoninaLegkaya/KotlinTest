package com.fb.roottest.data.repository

import android.content.Context
import androidx.room.Room
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.fb.roottest.data.db.AppDataBase
import com.commonsware.cwac.saferoom.SafeHelperFactory



class RepositoryFactory {
    companion object {
        fun provideRepository(context: Context): Repository {
//            val appDataBase: AppDataBase =
//                Room.databaseBuilder(context, AppDataBase::class.java, "database-app").fallbackToDestructiveMigration().build()

            val factory = SafeHelperFactory("passphraseField".toCharArray())
            val appDataBase = Room.databaseBuilder(context, AppDataBase::class.java, "database-app")
                .openHelperFactory(factory)
                .build()

            return RootRepository.getInstance(appDataBase)
        }
        fun encryptDataBase(context: Context){
          val state=  SQLCipherUtils.getDatabaseState(context,"database-app" )
            if(state== SQLCipherUtils.State.UNENCRYPTED){
                SQLCipherUtils.encrypt(context, "database-app","passphraseField".toCharArray())
            }
        }
        fun decryptDataBase(context: Context){
            val state=  SQLCipherUtils.getDatabaseState(context,"database-app" )
            if(state== SQLCipherUtils.State.ENCRYPTED){
                val dbFile = context.getDatabasePath("database-app")
                SQLCipherUtils.decrypt(context, dbFile,"passphraseField".toCharArray())
            }
        }

    }
}