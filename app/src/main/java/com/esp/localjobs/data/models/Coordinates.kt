package com.esp.localjobs.data.models

open class Coordinates(
    open var l: List<Double?> = listOf(null, null) // l[0] -> latitude, l[1] -> longitude
)