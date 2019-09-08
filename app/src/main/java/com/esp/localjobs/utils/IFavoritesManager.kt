package com.esp.localjobs.utils

import com.esp.localjobs.data.models.Job

interface IFavoritesManager {
    fun add(job: Job)
    fun remove(job: Job)
    suspend fun get(): Set<Job>
}