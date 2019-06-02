package com.esp.localjobs.DI

import com.esp.localjobs.data.base.BaseLocationRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository
import dagger.Module
import dagger.Provides

@Module
open class RepositoryModule {

    @Provides
    internal open fun provideJobRepository(): BaseLocationRepository<Job> {
        return JobsRepository()
    }
}