package com.esp.localjobs.data

import com.esp.localjobs.DI.RepositoryModule
import com.esp.localjobs.data.base.BaseLocationRepository
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.base.JobFilters
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Localizable
import com.esp.localjobs.viewModels.JobsViewModel
import dagger.Component

class MockRepository : BaseLocationRepository<Job> {
    override fun add(item: Job, callback: BaseRepository.EventCallback?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun update(id: String, newItem: Job, callback: BaseRepository.EventCallback?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(id: String, callback: BaseRepository.EventCallback?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(callback: BaseRepository.RepositoryCallback<Job>, filters: JobFilters?) {
        callback.onSuccess(fakeJobs)
    }

    override fun addLocationListener(
        coordinates: Localizable,
        range: Double,
        callback: BaseRepository.RepositoryCallback<Job>
    ) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun setItemLocation(id: String, coordinates: Localizable, callback: BaseRepository.EventCallback?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun patch(id: String, oldItem: Job, newItem: Job, callback: BaseRepository.EventCallback?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val NUMBER_OF_JOBS = 3
    }

    private val fakeJobs = 0.until(NUMBER_OF_JOBS).map { Job() }
}

class MockRepositoryModule : RepositoryModule() {

    override fun provideJobRepository(): BaseLocationRepository<Job> {
        return MockRepository()
    }
}

@Component(modules = [(MockRepositoryModule::class)])
interface TestViewModelInjector {
    fun inject(presenter: JobsViewModel)
}