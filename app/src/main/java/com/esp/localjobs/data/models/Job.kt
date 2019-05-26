package com.esp.localjobs.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    var title: String? = "",
    var description: String? = "",
    var g: String? = "", // geohashing of longitude, latitude
    var l: List<Double?> = listOf(null, null), // first: latitude, second: longitude
    var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
) : Parcelable