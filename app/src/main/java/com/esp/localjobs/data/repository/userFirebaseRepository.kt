package com.esp.localjobs.data.repository

import android.util.Log
import com.esp.localjobs.data.base.IUserRepository
import com.esp.localjobs.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@InternalCoroutinesApi
object userFirebaseRepository : IUserRepository {
    private const val TAG = "userFirebaseRepository"

    private val userCollection by lazy { FirebaseFirestore.getInstance().collection("user") }

    private fun getUserDocument(id: String) = userCollection.document(id)

    override suspend fun getUserDetails(id: String): User? =
        suspendCoroutine { continuation ->
            getUserDocument(id).get()
                .addOnSuccessListener {
                    try {
                        val user = it.toObject(User::class.java)
                        continuation.resume(user)
                    } catch (e: Exception) {
                        Log.e(TAG, "error in converting user: ${e.message}")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "error in converting user: ${it.message}")
                    continuation.resume(null)
                }
        }

    override suspend fun addUser(u: User): Unit =
        suspendCancellableCoroutine { continuation ->
            getUserDocument(u.uid).set(u)
                .addOnSuccessListener {
                    Log.d(TAG, "User information set")
                    continuation.completeResume(1)
                }
                .addOnFailureListener {
                    continuation.cancel(it)
                }
        }
}