package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.BaseLocationRepository
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import com.esp.localjobs.data.repository.JobsRepository
import com.google.firebase.auth.FirebaseAuth

class JobsViewModel : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>?>()
    private var repository = JobsRepository()

    val jobs: LiveData<List<Job>?>
        get() = _jobs

    // todo put filters as unique paramenter
    fun loadJobs(location: Location? = null, range: Double = 100.0, filter: JobsRepository.JobFilter? = null) {

        repository.jobFilter = filter

        when (location) {
            null ->
                repository.addListener(object : BaseRepository.RepositoryCallback<Job> {
                    override fun onSuccess(result: List<Job>) {
                        _jobs.postValue(result)
                    }

                    override fun onError(e: Exception) {
                        _jobs.postValue(null)
                    }
                })
            else ->
                repository.addLocationListener(
                    location,
                    range,
                    object : BaseRepository.RepositoryCallback<Job> {
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
}