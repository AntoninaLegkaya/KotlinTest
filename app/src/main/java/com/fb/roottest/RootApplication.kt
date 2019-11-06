package com.fb.roottest

import android.app.Application
import com.facebook.stetho.Stetho

class RootApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this);
    }
}