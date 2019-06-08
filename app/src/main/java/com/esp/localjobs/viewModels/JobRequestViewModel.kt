package com.esp.localjobs.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.base.IJobRequestRepository
import com.esp.localjobs.data.models.RequestToJob
import com.esp.localjobs.data.repository.jobRequesFirebaseRepository
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
class JobRequestViewModel : ViewModel() {

    private val requestRepository: IJobRequestRepository = jobRequesFirebaseRepository

    private val interestedUsers =
        mutableMapOf<String, MutableLiveData<List<String>>>() // jobid -> people interested ids

    fun addRequest(jobId: String, request: RequestToJob) =
        requestRepository.addRequest(jobId, request)

    fun getInterestedUserLiveData(jobId: String) =
        interestedUsers.getOrPut(jobId) { MutableLiveData() }

    fun startListeningForChanges(jobId: String) =
        requestRepository.listenForJobId(
            jobId,
            interestedUsers.getOrPut(jobId) { MutableLiveData() }
        )

    suspend fun hasSentInterest(userId: String, jobId: String) = requestRepository.hasSentInterest(userId, jobId)

    fun stopListeningForChanges(jobId: String) =
        requestRepository.stopListeningForJobId(jobId)
}