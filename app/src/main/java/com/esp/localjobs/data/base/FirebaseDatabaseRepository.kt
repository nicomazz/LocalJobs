package com.esp.localjobs.data.base

import android.util.Log
import com.esp.localjobs.data.models.Location
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType

abstract class FirebaseDatabaseRepository<Model> : BaseRepository<Model> {

    //    protected var db: Firebase
    private var firebaseCallback: FirebaseDatabaseRepositoryCallback<Model>? = null

    private lateinit var listener: BaseValueEventListener<Model>
    val db = FirebaseFirestore.getInstance()
    var collection = db.collection(getRootNode())
    var registration: ListenerRegistration? = null
    val geoFirestore = GeoFirestore(collection)
    var geoQuery: GeoQuery? = null
    val itemsList = ArrayList<Model>()

    abstract fun getRootNode(): String

    @Suppress("UNCHECKED_CAST")
    private val typeOfT = (javaClass
        .genericSuperclass as ParameterizedType)
        .actualTypeArguments[0] as Class<Model>


    override fun addListener(
        callback:  BaseRepository.RepositoryCallback<Model>,
        filter: ((Any) -> Any)?
    ) {
        (callback as? FirebaseDatabaseRepositoryCallback<Model>)?.let {
            firebaseCallback = it
            listener = BaseValueEventListener(it, typeOfT)
        } ?: throw Exception("Couldn't cast repository callback to firebase callback")

        registration?.remove()
        var dbCollection = collection
        filter?.let {
            dbCollection = filter(dbCollection) as CollectionReference
        }
        registration = dbCollection.addSnapshotListener(listener)
    }

    /**
     * Listen for jobs inside the circle defined by location and range.
     * @param location: center of the range of interest
     * @param range: maximum distance between @param location and a job
     * @param callback called on data update event or error
     */
    override fun addLocationListener(
        location: Location,
        range: Double,
        callback: BaseRepository.RepositoryCallback<Model>
    ) {
        // atm it can't hold >1 listeners
        geoQuery?.removeAllListeners()
        itemsList.clear()

        val geoQueryCenter = GeoPoint(location.latitude, location.longitude)
        geoQuery = geoFirestore.queryAtLocation(geoQueryCenter, range)

        (geoQuery as GeoQuery).addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
            override fun onDocumentEntered(p0: DocumentSnapshot?, p1: GeoPoint?) {
                try {

                    p0?.toObject(typeOfT)?.let {
                        if (!itemsList.contains(it))
                            itemsList.add(it)
                        callback.onSuccess(itemsList)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${p0?.data}")
                    throw e
                }
            }
            override fun onDocumentExited(p0: DocumentSnapshot?) {
                try {
                    p0?.toObject(typeOfT)?.let {
                        itemsList.remove(it)
                        callback.onSuccess(itemsList)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${p0?.data}")
                    throw e
                }
            }

            override fun onGeoQueryError(p0: java.lang.Exception?) {
                p0?.let {
                    callback.onError(it)
                }
            }
            // TODO we could recalculate distance from user and update it
            override fun onDocumentMoved(p0: DocumentSnapshot?, p1: GeoPoint?) { }
            override fun onDocumentChanged(p0: DocumentSnapshot?, p1: GeoPoint?) { }
            override fun onGeoQueryReady() { }
        })
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

    override fun patch(
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

    interface FirebaseDatabaseRepositoryCallback<T> : BaseRepository.RepositoryCallback<T>
}