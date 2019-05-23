package com.esp.localjobs.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

data class Job(
    var title: String? = "",
    var description: String? = "",
    var location: GeoPoint? = null,
    var city: String? = "",
    var salary: String? = "",
    var active: Boolean? = false,
    var uid: String? = ""
) : Parcelable {
    // todo use parcelize when geopoint will be parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        GeoPoint(parcel.readDouble(), parcel.readDouble()),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeDouble(location?.latitude ?: 0.0)
        parcel.writeDouble(location?.longitude ?: 0.0)
        parcel.writeString(city)
        parcel.writeString(salary)
        parcel.writeValue(active)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Job> {
        override fun createFromParcel(parcel: Parcel): Job {
            return Job(parcel)
        }

        override fun newArray(size: Int): Array<Job?> {
            return arrayOfNulls(size)
        }
    }
}