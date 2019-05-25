package com.esp.localjobs.data.base

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.lang.reflect.ParameterizedType

abstract class FirebaseDatabaseRepository<Model> : BaseRepository<Model> {

    //    protected var db: Firebase
    private var firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>
    val db = FirebaseFirestore.getInstance()
    var collection = db.collection(getRootNode())

    var registration: ListenerRegistration? = null

    abstract fun getRootNode(): String
    @Suppress("UNCHECKED_CAST")
    private val typeOfT = (javaClass
        .genericSuperclass as ParameterizedType)
        .actualTypeArguments[0] as Class<Model>

    override fun addListener(
        firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>,
        filter: ((CollectionReference) -> CollectionReference)?
    ) {
        this.firebaseCallback = firebaseCallback
        listener = BaseValueEventListener(firebaseCallback, typeOfT)
        registration?.remove()
        var dbCollection = collection
        filter?.let {
            dbCollection = filter(dbCollection)
        }
        registration = dbCollection.addSnapshotListener(listener)
    }

    fun removeListener() {
        registration?.remove()
    }

    /**
     * Add a document in the collection auto-generating an ID for it.
     * @param onSuccess called on add succeeded
     * @param onFailure called on add failure
     */
    override fun add(item: Model, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        collection.document()
            .set(item!!)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { error -> it(error) } }
            }
    }

    /**
     * Update document fields of a document in the collection with the given ID.
     * This function is recommended as consume less data traffic.
     * @param id document id
     * @param oldItem
     * @param newItem
     * @param onSuccess called on update succeeded
     * @param onFailure called on update failure
     */
    override fun update(
        id: String,
        oldItem: Model,
        newItem: Model,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {

        if (oldItem == newItem)
            return

        val updates = HashMap<String, Any?>()
        // update only different fields
        typeOfT.declaredFields.forEach {
            // isAccessible check it field is private
            if (it.isAccessible && it.get(oldItem) != it.get(newItem))
                updates[it.name] = it.get(newItem)
        }

        collection.document(id)
            .update(updates)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { exception -> it(exception) } }
            }
    }

    /**
     * Overwrite a document in the collection with an ID.
     * @param id document id
     * @param newItem will replace the old item
     * @param onSuccess called on update succeeded
     * @param onFailure called on update failure
     */
    override fun update(id: String, newItem: Model, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        collection.document(id)
            .set(newItem!!)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { exception -> it(exception) } }
            }
    }

    /**
     * Delete a document inside the collection given an ID
     * @param id document id
     * @param onSuccess called on delete succeeded
     * @param onFailure called on update failure
     */
    override fun delete(id: String, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        collection.document(id)
            .delete()
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { exception -> it(exception) } }
            }
    }

    interface FirebaseDatabaseRepositoryCallback<T> {
        fun onSuccess(result: List<T>)

        fun onError(e: Exception)
    }
}