package com.fb.roottest.base

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.InvocationTargetException

class ViewModelFactory : ViewModelProvider.NewInstanceFactory {

    private val application: Application


    private constructor(application: Application) {
        this.application = application
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (BaseViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                return modelClass.getConstructor(Application::class.java).newInstance(application)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }

        }
        return super.create(modelClass)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(application: Application): ViewModelFactory? {
            if (instance == null) {
                synchronized(ViewModelFactory::class.java) {
                    if (instance == null) {
                        instance = ViewModelFactory(application)
                    }
                }
            }
            return instance
        }
        @VisibleForTesting
        fun destriyinstance() {
            instance = null
        }

    }

}
