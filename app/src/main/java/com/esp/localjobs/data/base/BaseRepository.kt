package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.Location

interface BaseRepository<T> {

    /**
     * Add a document in the collection auto-generating an ID for it.
     * @param onSuccess called on add succeeded
     * @param onFailure called on add failure
     */
    fun add(
        item: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    /**
     * Update document fields of a document in the collection with the given ID, without fully overwriting it.
     * This function is recommended as consume less data traffic.
     * @param id document id
     * @param oldItem
     * @param newItem
     * @param onSuccess called on update succeeded
     * @param onFailure called on update failure
     */
    fun patch(
        id: String,
        oldItem: T,
        newItem: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    /**
     * Overwrite a document in the collection with an ID.
     * @param id document id
     * @param newItem will replace the old item
     * @param onSuccess called on update succeeded
     * @param onFailure called on update failure
     */
    fun update(
        id: String,
        newItem: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    /**
     * Delete a document inside the collection given an ID
     * @param id document id
     * @param onSuccess called on delete succeeded
     * @param onFailure called on update failure
     */
    fun delete(
        id: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    fun addListener(
        callback : RepositoryCallback<T>,
        filter: ((Any) -> Any)?
    )

    /**
     * Listen for items inside the circle defined by location and range.
     * @param location: center of the range of interest
     * @param range: maximum distance between @param location and a job
     * @param callback called on data update event or error
     */
    fun addLocationListener(
        location: Location,
        range: Double,
        callback: RepositoryCallback<T>
    )

    interface RepositoryCallback<T> {
        fun onSuccess(result: List<T>)
        fun onError(e: Exception)
    }
}
