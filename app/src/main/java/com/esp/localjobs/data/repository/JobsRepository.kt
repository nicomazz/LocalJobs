package com.esp.localjobs.data.repository

import com.esp.localjobs.data.base.FirebaseDatabaseLocationRepository
import com.esp.localjobs.data.models.Job

class JobsRepository : FirebaseDatabaseLocationRepository<Job>() {
    override fun getRootNode() = "jobs"
}
