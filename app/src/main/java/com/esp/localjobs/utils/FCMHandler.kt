package com.esp.localjobs.utils

import android.util.Log
import com.esp.localjobs.MyFirebaseMessagingService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId

object FCMHandler {
    private const val TAG = "FCMHandler"
    fun fetchAndSendFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token

                if (token == null) {
                    Log.e(TAG, "FCM messaging token is empty!")
                    return@OnCompleteListener
                }
                Log.d(TAG, token)
                MyFirebaseMessagingService.sendRegistrationToServer(token)
            })
    }
}