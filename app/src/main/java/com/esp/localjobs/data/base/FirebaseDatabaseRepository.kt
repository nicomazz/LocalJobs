package com.esp.localjobs.data.base

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


abstract class FirebaseDatabaseRepository<Model> {

    protected var databaseReference: DatabaseReference
    protected var firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>

    abstract fun getRootNode(): String

    init {
        databaseReference = FirebaseDatabase.getInstance().getReference(getRootNode())
    }

    fun addListener(firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>) {
        this.firebaseCallback = firebaseCallback
        listener = BaseValueEventListener(firebaseCallback)
        databaseReference.addValueEventListener(listener!!)
    }

    fun removeListener() {
        databaseReference.removeEventListener(listener)
    }

    interface FirebaseDatabaseRepositoryCallback<T> {
        fun onSuccess(result: List<T>)

        fun onError(e: Exception)
    }
}