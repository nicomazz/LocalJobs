package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.data.base.BaseLocationRepository
import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import javax.inject.Inject

class JobsViewModel : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>?>()
    @Inject
    lateinit var repository: BaseLocationRepository<Job> // = JobsRepository()

    val jobs: LiveData<List<Job>?>
        get() = _jobs

    init {
        LocalJobsApplication.components.inject(this)
    }

    fun loadJobs() {
        repository.addListener(object : FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Job> {
            override fun onSuccess(result: List<Job>) {
                _jobs.postValue(result)
            }

            override fun onError(e: Exception) {
                _jobs.postValue(null)
            }
        }, filters = null) /* { collectionToFilter ->
            // here we can do filtering
            collectionToFilter
        } */
    }

    fun loadJobs(location: Location, range: Double) {
        repository.addLocationListener(
            location,
            range,
            object : FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Job> {
                override fun onSuccess(result: List<Job>) {
                    _jobs.postValue(result)
                }

                override fun onError(e: Exception) {
                    _jobs.postValue(null)
                }
            }
        )
    }
}