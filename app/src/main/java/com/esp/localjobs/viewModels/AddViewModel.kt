package com.esp.localjobs.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository
import java.util.*

class AddViewModel : ViewModel() {
    enum class AddStatus {
        WAITING,
        SUCCESS,
        FAILURE
    }
    private val _status = MutableLiveData<AddStatus?>()
    val status: LiveData<AddStatus?>
            get() = _status

    fun addJobToRepository(job: Job) {
        job.id = UUID.randomUUID().toString()
        _status.value = AddStatus.WAITING
        JobsRepository().add(
            job,
            onSuccess = {
                _status.value = AddStatus.SUCCESS
            },
            onFailure = { e: Exception ->
                Log.e("AddViewModel", e.toString())
                _status.value = AddStatus.FAILURE
            }
        )
    }
}