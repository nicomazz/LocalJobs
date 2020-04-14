package com.esp.localjobs.data.models

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val mail: String = ""
) : Parcelable

fun FirebaseUser.toUser() = User(
    uid = uid,
    displayName = displayName ?: "Hidden name",
    phoneNumber = phoneNumber ?: "Hidden phone number",
    photoUrl = photoUrl?.toString() ?: "",
    mail = email ?: ""
)