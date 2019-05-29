package com.esp.localjobs.data.models

open class Location(
    open var l: List<Double> = listOf(0.0, 0.0), // l[0] -> latitude, l[1] -> longitude
    open var city: String? = ""
) : Coordinates {
    // more readable constructors
    constructor(latitude: Double, longitude: Double) : this(listOf(latitude, longitude))
    constructor(latitude: Double, longitude: Double, city: String?) : this(listOf(latitude, longitude), city)

    override fun latLng(): Pair<Double, Double> {
        return Pair(l[0], l[1])
    }
}

interface Coordinates {
    fun latLng(): Pair<Double, Double>
}