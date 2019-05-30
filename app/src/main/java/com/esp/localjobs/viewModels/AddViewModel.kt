package com.esp.localjobs.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository

class AddViewModel : ViewModel() {
    enum class AddStatus {
        WAITING,
        SUCCESS,
        FAILURE,
        NONE
    }
    private val _status = MutableLiveData<AddStatus?>()
    val status: LiveData<AddStatus?>
            get() = _status

    fun addJobToRepository(job: Job) {
        _status.value = AddStatus.WAITING
        JobsRepository().add(
            job,
            callback = object : BaseRepository.EventCallback {
                override fun onSuccess() {
                    _status.value = AddStatus.SUCCESS
                    _status.value = AddStatus.NONE
                }

                override fun onFailure(e: Exception) {
                    Log.e("AddViewModel", e.toString())
                    _status.value = AddStatus.FAILURE
                    _status.value = AddStatus.NONE
                }
            }
        )
    }
}