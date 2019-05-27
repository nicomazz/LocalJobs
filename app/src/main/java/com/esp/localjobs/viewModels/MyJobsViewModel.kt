package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint

class MyJobsViewModel : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>?>()
    private var repository = JobsRepository()

    val jobs: LiveData<List<Job>?>
        get() = _jobs

    fun loadJobs() {
        repository.addListener(object : FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Job> {
            override fun onSuccess(result: List<Job>) {
                _jobs.postValue(result)
            }

            override fun onError(e: Exception) {
                _jobs.postValue(null)
            }
        }) { collectionToFilter ->
            collectionToFilter.whereEqualTo("uid", FirebaseAuth.getInstance().currentUser.uid)
        }
    }
}