package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.Coordinates
import com.esp.localjobs.data.models.Location

interface BaseLocationRepository<T> : BaseRepository<T> {

    /**
     * Listen for items inside the circle defined by location and range.
     * @param location: center of the range of interest
     * @param range: maximum distance between @param location and a job
     * @param callback called on data update event or error
     */
    fun addLocationListener(
        coordinates: Coordinates,
        range: Double,
        callback: BaseRepository.RepositoryCallback<T>
    )

    /**
     * Set location of an item with given id, eventually overwriting it.
     */
    fun setItemLocation(
        id: String,
        coordinates: Coordinates,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )
}