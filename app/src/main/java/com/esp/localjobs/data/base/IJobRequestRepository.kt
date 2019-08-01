package com.esp.localjobs.data.base

import androidx.lifecycle.MutableLiveData
import com.esp.localjobs.data.models.RequestToJob

interface IJobRequestRepository {
    fun listenForJobId(jobId: String, userIds: MutableLiveData<List<String>>)
    fun stopListeningForJobId(id: String)
    fun addRequest(jobId: String, request: RequestToJob)
    suspend fun hasSentInterest(userId: String, jobId: String): Boolean
}