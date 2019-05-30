package com.esp.localjobs.data.models

open class Location(
    open var l: List<Double> = listOf(0.0, 0.0), // l[0] -> latitude, l[1] -> longitude
    open var city: String? = ""
) : Localizable {
    // more readable constructor
    constructor(latitude: Double, longitude: Double, city: String? = "") : this(listOf(latitude, longitude), city)

    override fun latLng(): Pair<Double, Double> {
        return Pair(l[0], l[1])
    }
}

interface Localizable {
    fun latLng(): Pair<Double, Double>
}