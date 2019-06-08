package com.esp.localjobs.data.models

data class RequestToJob(
    val job_publisher_id: String = "",
    val name: String = "",
    val interested_user_id: String = "",
    val job_id: String = ""
)