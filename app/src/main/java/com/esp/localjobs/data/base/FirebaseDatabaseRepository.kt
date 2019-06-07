package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.Identifiable
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.lang.reflect.ParameterizedType

abstract class FirebaseDatabaseRepository<Model : Identifiable> : BaseRepository<Model> {

    //    protected var db: Firebase
    private var firebaseCallback: BaseRepository.RepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>
    val db = FirebaseFirestore.getInstance()
    var collection = db.collection(getRootNode())
    var registration: ListenerRegistration? = null

    abstract fun getRootNode(): String

    /**
     * Override this filter and use firestore query to include/exclude items
     */
    abstract fun filter(collection: CollectionReference) : Query?

    @Suppress("UNCHECKED_CAST")
    val typeOfT = (javaClass
        .genericSuperclass as ParameterizedType)
        .actualTypeArguments[0] as Class<Model>

    override fun addListener(callback: BaseRepository.RepositoryCallback<Model>) {
        firebaseCallback = callback
        listener = BaseValueEventListener(callback, typeOfT)

        registration?.remove()
        val dbCollection = collection

        // filter if required
        registration = filter(dbCollection)?.addSnapshotListener(listener)
            ?: dbCollection.addSnapshotListener(listener)
    }

    fun removeListener() {
        registration?.remove()
    }

    override fun add(
        item: Model,
        callback: BaseRepository.EventCallback?
    ) {
        item.id = collection.document().id
        collection.document(item.id)
            .set(item)
            .addOnSuccessListener { callback?.onSuccess() }
            .addOnFailureListener { e -> callback?.onFailure(e) }
    }

    override fun update(
        id: String,
        newItem: Model,
        callback: BaseRepository.EventCallback?
    ) {
        collection.document(id)
            .set(newItem)
            .addOnSuccessListener { callback?.onSuccess() }
            .addOnFailureListener { e -> callback?.onFailure(e) }
    }

    override fun delete(
        id: String,
        callback: BaseRepository.EventCallback?
    ) {
        collection.document(id)
            .delete()
            .addOnSuccessListener { callback?.onSuccess() }
            .addOnFailureListener { e -> callback?.onFailure(e) }
    }

    companion object {
        const val TAG = "FirebaseDatabaseRepository"
    }
}