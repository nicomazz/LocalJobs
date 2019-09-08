package com.esp.localjobs.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.esp.localjobs.LocalJobsApplication
import com.esp.localjobs.data.base.BaseRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.repository.JobsRepository

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
        Log.d("favorites", "adding: ${job.id}")
        sharedPreferences.edit(commit = true) {
            // stringSet is bugged so i must do this :/
            remove(FAV_KEY)
            apply()
            putStringSet(FAV_KEY, favKeys)
            apply()
        }
        favorites?.add(job)
    }

    override fun remove(job: Job) {
        val favKeys = sharedPreferences.getStringSet(FAV_KEY, mutableSetOf<String>()) ?: return
        favKeys.remove(job.id)
        Log.d("favorites", "removing: ${job.id}")
        sharedPreferences.edit(commit = true) {
            // stringSet is bugged so i must do this :/
            remove(FAV_KEY)
            apply()
            putStringSet(FAV_KEY, favKeys)
            apply()
        }
        favorites?.remove(job)
    }

    override suspend fun get(): Set<Job> {
        if (favorites == null) {
            favorites = load()
        }
        return (favorites as MutableSet<Job>).toSet()
    }

    private suspend fun load(): MutableSet<Job> {
        val favKeys = sharedPreferences.getStringSet(FAV_KEY, mutableSetOf<String>())
        Log.d("favorites", "loading: $favKeys")
        val favList = mutableSetOf<Job>()
        favKeys?.forEach { key -> loader.get(key)?.let { favList.add(it) } }
        return favList
    }
}