package com.esp.localjobs.data.base

import com.google.firebase.firestore.CollectionReference

interface BaseRepository<T> {
    fun add(
        item: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    // doesn't overwrite the entire document
    fun update(
        id: String,
        oldItem: T,
        newItem: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    fun update(
        id: String,
        newItem: T,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    fun delete(
        id: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((e: Exception) -> Unit)? = null
    )

    fun addListener(
        firebaseCallback: FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<T>,
        filter: ((CollectionReference) -> CollectionReference)?
    )

}
