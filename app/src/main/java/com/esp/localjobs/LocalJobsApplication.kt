package com.esp.localjobs

import android.app.Application
import android.content.Context

class LocalJobsApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: LocalJobsApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}