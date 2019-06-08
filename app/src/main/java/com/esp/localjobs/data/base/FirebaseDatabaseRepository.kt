package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.Identifiable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.lang.reflect.ParameterizedType

abstract class FirebaseDatabaseRepository<Model : Identifiable> : BaseRepository<Model> {

    //    protected var db: Firebase
    private var firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>
    val db = FirebaseFirestore.getInstance()
    var collection = db.collection(getRootNode())
    var registration: ListenerRegistration? = null

    abstract fun getRootNode(): String

    @Suppress("UNCHECKED_CAST")
    val typeOfT = (javaClass
        .genericSuperclass as ParameterizedType)
        .actualTypeArguments[0] as Class<Model>

    override fun addListener(
        callback: BaseRepository.RepositoryCallback<Model>,
        filters: JobFilters?
    ) {
        (callback as? FirebaseDatabaseRepositoryCallback<Model>)?.let {
            firebaseCallback = it
            listener = BaseValueEventListener(it, typeOfT)
        } ?: throw Exception("Couldn't cast repositoryUserRepository callback to firebase callback")

        registration?.remove()
        val dbCollection = collection
        // todo implement filtering using JobFilters
        /*  filter?.let {
              dbCollection = filter(dbCollection) as CollectionReference
          }*/
        // dbCollection.ad

        registration = dbCollection.addSnapshotListener(listener)
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

    interface FirebaseDatabaseRepositoryCallback<T> : BaseRepository.RepositoryCallback<T>

    companion object {
        const val TAG = "FirebaseDatabaseRepository"
    }
}