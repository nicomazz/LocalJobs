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
    val jobs = ArrayList<Job>()
    val  geoFirestore = GeoFirestore(collection)
    var geoQuery: GeoQuery? = null

    /**
     * Listen for jobs inside the circle defined by location and range.
     * @param location: center of the range of interest
     * @param range: maximum distance between @param location and a job
     * @param callback called on data update event or error
     */
    fun addLocationListener(
        location: GeoPoint,
        range: Double,
        callback: FirebaseDatabaseRepositoryCallback<Job>
    ) {
        geoQuery?.removeAllListeners()
        jobs.clear()
        geoQuery = geoFirestore.queryAtLocation(location, range)

        (geoQuery as GeoQuery).addGeoQueryDataEventListener( object : GeoQueryDataEventListener {
            override fun onDocumentEntered(p0: DocumentSnapshot?, p1: GeoPoint?) {
                try {
                    p0?.toObject(Job::class.java)?.let {
                        if (!jobs.contains(it))
                            jobs.add(it)
                        callback.onSuccess(jobs)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${p0?.data.toString()}")
                }
            }
            override fun onDocumentExited(p0: DocumentSnapshot?) {
                try {
                    p0?.toObject(Job::class.java)?.let {
                        jobs.remove(it)
                        callback.onSuccess(jobs)
                    }
                } catch (e: RuntimeException) {
                    Log.d("JobsRepository", "Could not deserialize ${p0?.data.toString()}")
                }
            }

            override fun onGeoQueryError(p0: Exception?) {
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
}
