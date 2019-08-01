package com.esp.localjobs

import android.app.Application
import android.content.Context

class LocalJobsApplication : Application() {

    companion object {
        lateinit var instance: LocalJobsApplication
                private set

        fun applicationContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}