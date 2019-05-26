package com.esp.localjobs.DI

import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository
import dagger.Module
import dagger.Provides

@Module
open class RepositoryModule {

    @Provides
    internal open fun provideJobRepository(): BaseRepository<Job> {
        return JobsRepository()
    }
}