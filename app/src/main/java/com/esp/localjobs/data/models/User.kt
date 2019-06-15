package com.esp.localjobs.data.models

import com.google.firebase.auth.FirebaseUser

data class User(
    val uid: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val mail: String = ""
)

fun FirebaseUser.toUser() = User(
    uid = uid,
    displayName = displayName ?: "Hidden name",
    phoneNumber = phoneNumber ?: "Hidden phone number",
    photoUrl = photoUrl?.toString() ?: "",
    mail = email ?: ""
)