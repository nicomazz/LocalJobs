package com.esp.localjobs.models

import com.google.firebase.firestore.GeoPoint

data class Job(
    var uid: String? = "",
    var city: String? = "",
    var active: Boolean? = false,
    var location: GeoPoint? = null,
    var salary: String? = "",
    var title: String? = "",
    var desc: String? = ""
){
}