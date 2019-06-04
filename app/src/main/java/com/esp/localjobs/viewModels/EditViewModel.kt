package com.esp.localjobs.viewModels

import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository

class EditViewModel : ViewModel() {
    fun patch(
        oldJob: Job,
        newJob: Job,
        onSuccess: (() -> Unit),
        onFailure: ((e: Exception) -> Unit)
    ) {
        JobsRepository().patch(oldJob.id, oldJob, newJob, callback = object : BaseRepository.EventCallback {
            override fun onSuccess() {
                onSuccess.invoke()
            }

            override fun onFailure(e: Exception) {
                onFailure.invoke(e)
            }
        })
    }

    fun delete(
        id: String,
        onSuccess: (() -> Unit),
        onFailure: ((e: Exception) -> Unit)
    ) {
        JobsRepository().delete(id, callback = object : BaseRepository.EventCallback {
            override fun onSuccess() {
                onSuccess.invoke()
            }

            override fun onFailure(e: Exception) {
                onFailure.invoke(e)
            }
        })
    }
}