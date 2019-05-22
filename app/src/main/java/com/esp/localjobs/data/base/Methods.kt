package com.esp.localjobs.data.base

interface Methods<T> {
    fun add(item: T, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)
    // doesn't overwrite the entire document
    fun update(id: String, oldItem: T, newItem: T, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)
    fun update(id: String, newItem: T, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)
    fun delete(id: String, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)
}
