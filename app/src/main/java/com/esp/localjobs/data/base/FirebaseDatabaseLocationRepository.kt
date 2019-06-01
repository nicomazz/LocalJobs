package com.esp.localjobs.data.base

import android.util.Log
import com.esp.localjobs.data.models.Identifiable
import com.esp.localjobs.data.models.Localizable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener

abstract class FirebaseDatabaseLocationRepository<Model> :
    FirebaseDatabaseRepository<Model>(),
    BaseLocationRepository<Model>
        where Model : Identifiable, Model : Localizable {

    val geoFirestore = GeoFirestore(collection)
    var geoQuery: GeoQuery? = null
    // todo substitute this with a better data structure
    val itemsList = ArrayList<Model>()

    override fun addLocationListener(
        coordinates: Localizable,
        range: Double,
        callback: BaseRepository.RepositoryCallback<Model>
    ) {
        // atm it can't hold >1 listeners
        geoQuery?.removeAllListeners()
        itemsList.clear()

        val geoQueryCenter = coordinatesToGeoPoint(coordinates)
        geoQuery = geoFirestore.queryAtLocation(geoQueryCenter, range)

        geoQuery?.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
            override fun onDocumentEntered(document: DocumentSnapshot?, position: GeoPoint?) {
                try {

                    document?.toObject()?.let {
                        if (!itemsList.contains(it))
                            itemsList.add(it)
                        callback.onSuccess(itemsList)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${document?.data}")
                    throw e
                }
            }
            override fun onDocumentExited(document: DocumentSnapshot?) {
                try {
                    document?.toObject()?.let {
                        itemsList.remove(it)
                        callback.onSuccess(itemsList)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${document?.data}")
                    throw e
                }
            }
            override fun onGeoQueryError(e: java.lang.Exception?) {
                e?.let {
                    callback.onError(it)
                }
            }
            override fun onDocumentMoved(document: DocumentSnapshot?, position: GeoPoint?) {}
            override fun onDocumentChanged(document: DocumentSnapshot?, position: GeoPoint?) {}
            override fun onGeoQueryReady() {}
        })
    }

    /**
     * As we are using GeoFirestore, we need to encode latitude and longitude using geohashing.
     * This solution uses a two-step addition which is not optimal ( an interruption / error in the middle of the
     * addition might leave inconsistent data in out remote db )
     */
    override fun add(
        item: Model,
        callback: BaseRepository.EventCallback?
    ) {
        item.id = collection.document().id
        collection.document(item.id)
            .set(item)
            .addOnSuccessListener {
                // once the job has been added, set GeoFirestore location
                setItemLocation(
                    item.id,
                    coordinates = item as Localizable,
                    callback = object : BaseRepository.EventCallback {
                        override fun onSuccess() { callback?.onSuccess() }
                        override fun onFailure(e: Exception) {
                            delete(item.id)
                            callback?.onFailure(e)
                        }
                    }
                )
            }
            .addOnFailureListener { exception ->
                callback?.onFailure(exception)
            }
    }

    override fun update(
        id: String,
        newItem: Model,
        callback: BaseRepository.EventCallback?
    ) {
        // as this function overwrite the entire document, just recreate it
        super.delete(
            id,
            callback = object : BaseRepository.EventCallback {
                override fun onSuccess() {
                    add(item = newItem, callback = callback)
                }

                override fun onFailure(e: Exception) { callback?.onFailure(e) }
            }

        )
    }

    override fun setItemLocation(
        id: String,
        coordinates: Localizable,
        callback: BaseRepository.EventCallback?
    ) {
        val geoPoint = coordinatesToGeoPoint(coordinates)
        geoFirestore.setLocation(id, geoPoint) { geoException ->
            if (geoException == null)
                callback?.onSuccess()
            else
                callback?.onFailure(geoException)
        }
    }

    private fun coordinatesToGeoPoint(coordinates: Localizable): GeoPoint {
        val (lat, lng) = coordinates.latLng()
        return GeoPoint(lat, lng)
    }

    private fun DocumentSnapshot.toObject() = toObject(typeOfT)
}
