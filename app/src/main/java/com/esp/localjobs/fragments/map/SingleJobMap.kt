package com.esp.localjobs.fragments.map

import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location

class SingleJobMap : JobsMapFragment() {

    override fun getJobsToDisplay(): List<Job> {
        return job?.let { listOf(it) } ?: listOf()
    }

    override fun provideStartLocation(): Location? {
        return job?.let { Location(it.latitude(), it.longitude()) }
    }

    val job by lazy { arguments?.getParcelable<Job>("job") }
}