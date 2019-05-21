package com.esp.localjobs.data.repository

import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job

class JobsRepository : FirebaseDatabaseRepository<Job>() {
    override fun getRootNode() = "jobs"
}