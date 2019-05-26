package com.esp.localjobs.DI

import com.esp.localjobs.viewModels.JobsViewModel
import dagger.Component

@Component(modules = [(RepositoryModule::class)])
interface ViewModelInjector {
    fun inject(presenter: JobsViewModel)
}