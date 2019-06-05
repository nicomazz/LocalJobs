package com.esp.localjobs.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository

class AddViewModel : ViewModel() {
    fun addJobToRepository(
        job: Job,
        onSuccess: (() -> Unit),
        onFailure: (() -> Unit)
    ) {
        JobsRepository().add(
            job,
            callback = object : BaseRepository.EventCallback {
                override fun onSuccess() {
                    onSuccess()
                }
                override fun onFailure(e: Exception) {
                    Log.e("AddViewModel", e.toString())
                    onFailure()
                }
            }
        )
    }
}