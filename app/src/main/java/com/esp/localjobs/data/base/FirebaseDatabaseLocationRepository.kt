package com.esp.localjobs.data.base

import android.util.Log
import com.esp.localjobs.data.models.Coordinates
import com.esp.localjobs.data.models.Identifiable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.lang.RuntimeException
import kotlin.Exception

abstract class FirebaseDatabaseLocationRepository<Model> :
    FirebaseDatabaseRepository<Model>(),
    BaseLocationRepository<Model>
        where Model : Identifiable, Model : Coordinates {

    val geoFirestore = GeoFirestore(collection)
    var geoQuery: GeoQuery? = null
    val itemsList = ArrayList<Model>()

    override fun addLocationListener(
        coordinates: Coordinates,
        range: Double,
        callback: BaseRepository.RepositoryCallback<Model>
    ) {
        // atm it can't hold >1 listeners
        geoQuery?.removeAllListeners()
        itemsList.clear()

        val geoQueryCenter = coordinatesToGeoPoint(coordinates)
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

    /**
     * As we are using GeoFirestore, we need to encode latitude and longitude using geohashing.
     * This solution uses a two-step addition which is not optimal ( an interruption / error in the middle of the
     * addition might leave inconsistent data in out remote db )
     */
    override fun add(
        item: Model,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {
        if (item.id.isEmpty()) {
            // delegate to Firebase the assignment of an ID
            item.id = collection.document().id
        }
        collection.document(item.id)
            .set(item)
            .addOnSuccessListener {
                // once the job has been added, set GeoFirestore location
                setItemLocation(
                    item.id,
                    // coordinates = Location(coords.l[0]!!, coords.l[1]!!),
                    coordinates = item as Coordinates,
                    onSuccess = onSuccess,
                    onFailure = { exception ->
                        delete(item.id) // try to delete inconsistent data
                        onFailure?.invoke(exception)
                    }
                )
            }
            .addOnFailureListener { exception ->
                onFailure?.invoke(exception)
            }
    }

    override fun update(id: String, newItem: Model, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        // as this function overwrite the entire document, just recreate it
        super.delete(
            id,
            onSuccess = {
                add(
                    item = newItem,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    override fun setItemLocation(
        id: String,
        coordinates: Coordinates,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {
        val geoPoint = coordinatesToGeoPoint(coordinates)
        geoFirestore.setLocation(id, geoPoint) { geoException ->
            if (geoException == null)
                onSuccess?.invoke()
            else
                onFailure?.invoke(geoException)
        }
    }

    private fun coordinatesToGeoPoint(coordinates: Coordinates): GeoPoint {
        val (lat, lng) = coordinates.latLng()
        return GeoPoint(lat, lng)
    }
}