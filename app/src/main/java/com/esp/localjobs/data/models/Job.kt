package com.esp.localjobs.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    override var id: String = "",
    var title: String? = "",
    var description: String? = "",
    override var l: List<Double> = listOf(0.0, 0.0), // l[0] -> latitude, l[1] -> longitude
    override var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
) : Parcelable, Identifiable, Location(listOf(l[0], l[1]))