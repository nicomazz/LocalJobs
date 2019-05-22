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

    override fun add(item: Model, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        collection.document()
            .set(item!!)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { error -> it(error) } }
            }
    }

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

    override fun update(id: String, newItem: Model, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        collection.document(id)
            .set(newItem!!)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener { it() } }
                onFailure?.let { task.addOnFailureListener { exception -> it(exception) } }
            }
    }

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