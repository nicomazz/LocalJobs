package com.esp.localjobs

import android.app.Application
import com.esp.localjobs.viewModels.DaggerViewModelInjector
import com.esp.localjobs.DI.RepositoryModule
import com.esp.localjobs.DI.ViewModelInjector

open class LocalJobsApplication : Application() {

    companion object {
        var components: ViewModelInjector = DaggerViewModelInjector.builder()
            .repositoryModule(RepositoryModule())
            .build()
    }
}