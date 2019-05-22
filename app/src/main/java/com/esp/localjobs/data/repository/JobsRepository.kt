package com.esp.localjobs.data.repository

import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import java.lang.Exception

class JobsRepository :  FirebaseDatabaseRepository<Job>() {
    override fun getRootNode() = "jobs"

}
