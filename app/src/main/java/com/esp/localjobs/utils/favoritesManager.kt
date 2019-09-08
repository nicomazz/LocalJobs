package com.esp.localjobs.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object favoritesManager : IFavoritesManager {
    private const val FAV_KEY = "favourites_ids"

    private val loader: BaseRepository<Job> by lazy { JobsRepository() }
    private val sharedPreferences: SharedPreferences by lazy {
        LocalJobsApplication.applicationContext()
            .getSharedPreferences("favorites", Context.MODE_PRIVATE)
    }

    private var favorites: MutableSet<Job>? = null

    override fun add(job: Job) {
        val favKeys = sharedPreferences.getStringSet(FAV_KEY, mutableSetOf<String>())
            ?: mutableSetOf<String>()
        favKeys.add(job.id)
        sharedPreferences.edit(commit = true) {
            putStringSet(FAV_KEY, favKeys)
        }
        favorites?.add(job)
    }

    override fun remove(job: Job) {
        val favKeys = sharedPreferences.getStringSet(FAV_KEY, null) ?: return
        favKeys.remove(job.id)
        sharedPreferences.edit(commit = true) {
            putStringSet(FAV_KEY, favKeys)
        }
        favorites?.remove(job)
    }

    override suspend fun get(): Set<Job> {
        if (favorites == null) {
            favorites = load()
        }
        return favorites as Set<Job>
    }

    private suspend fun load(): MutableSet<Job> {
        val favKeys = sharedPreferences.getStringSet(FAV_KEY, mutableSetOf<String>())
        val favList = mutableSetOf<Job>()
        favKeys?.forEach { key -> loader.get(key)?.let { favList.add(it) } }
        return favList
    }
}