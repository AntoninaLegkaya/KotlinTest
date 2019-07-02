package com.fb.roottest.splash

import android.app.Application
import com.fb.roottest.base.BaseViewModel
import com.fb.roottest.base.SingleLiveEvent
import com.fb.roottest.data.repository.Repository
import java.util.Timer
import kotlin.concurrent.schedule

const val DELAY = 750L

class SplashViewModel(application: Application, repository: Repository) : BaseViewModel(application, repository) {

    val navigateHomeFragment = SingleLiveEvent<Unit>()

    fun init() {
        Timer(true).schedule(DELAY) {
            navigateHomeFragment.postValue( Unit)
        }
    }

}