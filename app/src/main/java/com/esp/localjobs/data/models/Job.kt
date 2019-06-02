package com.esp.localjobs.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    override var id: String = "",
    var title: String? = "",
    var description: String? = "",
    // same name used by GeoFirestore, adopted to avoid duplicating data
    var l: List<Double> = listOf(0.0, 0.0), // l[0] -> latitude, l[1] -> longitude
    var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
) : Parcelable, Identifiable, Localizable {
    override fun latLng(): Pair<Double, Double> {
        return Pair(l[0], l[1])
    }
    fun getLatitude() = l[0]

    fun getLongitude() = l[1]
}