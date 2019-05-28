package com.esp.localjobs.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    var title: String? = "",
    var description: String? = "",
    override var l: List<Double?> = listOf(null, null), // l[0] -> latitude, l[1] -> longitude
    var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
) : Coordinates(listOf(l[0], l[1])), Parcelable