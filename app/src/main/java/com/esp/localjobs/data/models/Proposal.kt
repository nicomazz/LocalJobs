package com.esp.localjobs.data.models

data class Proposal(
    var uid: String? = "",
    var city: String? = "",
    var range: Int? = 0,
    var active: Boolean? = false,
    var location: Location?,
    var salary: Int? = 0,
    var title: String? = "",
    var desc: String? = ""
)