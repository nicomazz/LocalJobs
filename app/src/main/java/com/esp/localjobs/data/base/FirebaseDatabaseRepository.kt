package com.esp.localjobs.data.base

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.lang.reflect.ParameterizedType
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T






abstract class FirebaseDatabaseRepository<Model> {

    //    protected var db: Firebase
    protected var firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>
    val db = FirebaseFirestore.getInstance()
    abstract fun getRootNode(): String

    private val typeOfT = (javaClass
        .genericSuperclass as ParameterizedType)
        .actualTypeArguments[0] as Class<Model>

    var registration: ListenerRegistration? = null

    fun addListener(firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>) {
        this.firebaseCallback = firebaseCallback
        listener = BaseValueEventListener(firebaseCallback,typeOfT)
        registration?.remove()
        // todo uncomment this to query the database
        registration = db.collection(getRootNode()).addSnapshotListener(listener)
    }

    fun removeListener() {
        registration?.remove()
    }


    interface FirebaseDatabaseRepositoryCallback<T> {
        fun onSuccess(result: List<T>)

        fun onError(e: Exception)
    }

}