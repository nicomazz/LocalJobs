package com.esp.localjobs.data.models

import com.google.firebase.firestore.GeoPoint

data class Job(
    var title: String? = "",
    var description: String? = "",
    var location: GeoPoint? = null,
    var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
)