package com.esp.localjobs.models

data class Job(
    var uid: String? = "",
    var city: String? = "",
    var active: Boolean? = false,
    var location: Location?,
    var salary: Int? = 0,
    var title: String? = "",
    var desc: String? = ""
)