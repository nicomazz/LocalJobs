package com.esp.localjobs.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.esp.localjobs.data.base.IJobRequestRepository
import com.esp.localjobs.data.models.RequestToJob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object jobRequesFirebaseRepository : IJobRequestRepository {

    private const val TAG = "JobRequestFirebaseRepo"

    override fun addRequest(jobId: String, request: RequestToJob) {
        getRequestsCollection(jobId)
            .document(request.interested_user_id)
            .set(request)
    }

    private val listeners: HashMap<String, ListenerRegistration> = hashMapOf()

    private fun getRequestsCollection(jobId: String) =
        FirebaseFirestore.getInstance().collection("jobs")
            .document(jobId).collection("requests")

    override fun listenForJobId(jobId: String, userIds: MutableLiveData<List<String>>) {
        listeners[jobId] = getRequestsCollection(jobId)
            .addSnapshotListener { it, _ ->
                val usersIds = it?.map {
                    it.toObject(RequestToJob::class.java)
                }
                userIds.postValue(usersIds?.map { it.interested_user_id })
            }
    }

    override fun stopListeningForJobId(id: String) {
        listeners[id]?.remove()
    }

    override suspend fun hasSentInterest(userId: String, jobId: String): Boolean =
            suspendCoroutine { continuation ->
                getRequestsCollection(jobId)
                    .document(userId).get()
                    .addOnSuccessListener {
                        continuation.resume(it.exists())
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Request failed")
                        continuation.resume(false)
                    }
            }
}