package com.esp.localjobs.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class is used to represent both jobs and proposals. The only difference between the two objects is the field
 * range: a job doesn't have it. Also, the field salary must be interpreted as "cost" in case of job, "gain" in case of
 * proposal.
 */

@Parcelize
data class Job(
    override var id: String = "",
    var title: String? = "",
    var description: String? = "",
    // same name used by GeoFirestore, adopted to avoid duplicating data
    var l: List<Double> = listOf(0.0, 0.0), // l[0] -> latitude, l[1] -> longitude
    var city: String? = "",
    var range: Int? = null,
    var salary: String? = "",
    var active: Boolean? = false,
    var isJob: Boolean? = true,
    var uid: String? = ""
) : Parcelable, Identifiable, Localizable {
    override fun latLng(): Pair<Double, Double> {
        return Pair(l[0], l[1])
    }
    // named without 'get' prefix because firebase would push an attribute with key "latitude" duplicating data
    fun latitude() = l[0]
    fun longitude() = l[1]
}