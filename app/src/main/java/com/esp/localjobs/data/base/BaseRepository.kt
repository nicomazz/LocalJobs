package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.Identifiable

/**
 * This interface define a basic sets of method available for any object that provides an ID.
 */
interface BaseRepository<T> where T : Identifiable {

    /**
     * Add a document in the collection auto-generating an ID for it.
     * @param onSuccess called on add succeeded
     * @param onFailure called on add failure
     */
    fun add(
        item: T,
        callback: EventCallback? = null
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
        callback: EventCallback? = null
    )

    /**
     * Delete a document inside the collection given an ID
     * @param id document id
     * @param onSuccess called on delete succeeded
     * @param onFailure called on update failure
     */
    fun delete(
        id: String,
        callback: EventCallback? = null
    )

    fun addListener(
        callback: RepositoryCallback<T>
    )

    interface RepositoryCallback<T> {
        fun onSuccess(result: List<T>)
        fun onError(e: Exception)
    }

    interface EventCallback {
        fun onSuccess()
        fun onFailure(e: Exception)
    }
}