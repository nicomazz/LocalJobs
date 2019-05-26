package com.esp.localjobs.data

import com.esp.localjobs.DI.RepositoryModule
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.viewModels.JobsViewModel
import com.google.firebase.firestore.CollectionReference
import dagger.Component

class MockRepository : BaseRepository<Job> {

    companion object {
        const val NUMBER_OF_JOBS = 3
    }

    private val fakeJobs = 0.until(NUMBER_OF_JOBS).map { Job() }
    override fun add(item: Job, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(
        id: String,
        oldItem: Job,
        newItem: Job,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(id: String, newItem: Job, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(id: String, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(
        firebaseCallback: FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Job>,
        filter: ((CollectionReference) -> CollectionReference)?
    ) {
        firebaseCallback.onSuccess(fakeJobs)
    }
}


class MockRepositoryModule : RepositoryModule() {

    override fun provideJobRepository(): BaseRepository<Job> {
        return MockRepository()
    }
}

@Component(modules = [(MockRepositoryModule::class)])
interface TestViewModelInjector {
    fun inject(presenter: JobsViewModel)
}