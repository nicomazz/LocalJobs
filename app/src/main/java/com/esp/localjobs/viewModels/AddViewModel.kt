package com.esp.localjobs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository

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
        _status.value = AddStatus.WAITING
        JobsRepository().add(
            job,
            onSuccess = {
                _status.value = AddStatus.SUCCESS

            },
            onFailure = { e: Exception ->
                _status.value = AddStatus.FAILURE
            }
        )
    }
}