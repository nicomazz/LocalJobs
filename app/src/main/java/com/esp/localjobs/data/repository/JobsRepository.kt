package com.esp.localjobs.data.repository

import android.util.Log
import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.models.Job
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.lang.Exception
import java.lang.RuntimeException

class JobsRepository : FirebaseDatabaseRepository<Job>() {
    override fun getRootNode() = "jobs"


    /**
     * // TODO generalize this: we don't know if an item (Model) uses location -> find a way
     * As we are using GeoFirestore, we need to encode latitude and longitude using geohashing.
     * This solution uses a two-step addition which is not optimal ( an interruption / error in the middle of the
     * addition might leave inconsistent data in out remote db )
     */
    override fun add(
        item: Job,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {
        val id = collection.document().id
        collection.document(id)
            .set(item)
            .addOnSuccessListener {
                // once the job has been added, set hashed location
                geoFirestore.setLocation(id, GeoPoint(item.l[0]!!, item.l[1]!!)) { geoException ->
                    if (geoException == null)
                        onSuccess?.invoke()
                    else { // if there has been an error with hashed location, try to delete inconsistent data
                        collection.document(id).delete()
                        onFailure?.invoke(geoException)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure?.invoke(exception)
            }
    }
}
