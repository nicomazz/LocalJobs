package com.esp.localjobs.data.repository

import com.esp.localjobs.data.base.FirebaseDatabaseLocationRepository
import com.esp.localjobs.data.models.Job
import com.esp.localjobs.data.models.Location
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query

const val MAX_RANGE_KM = 100

class JobsRepository : FirebaseDatabaseLocationRepository<Job>() {
    override fun getRootNode() = "jobs"

    /**
     * Set this property to add constrains. Might be moved into constructor and observed in the future
     */
    var jobFilter: JobFilter? = null

    override fun filter(collection: CollectionReference): Query? {
        val filter = jobFilter ?: return null

        var query = collection.whereEqualTo("itIsJob", filter.filteringJobs)
        filter.uid?.let {
            query = query.whereEqualTo("uid", it)
        }
        filter.salary?.let {
            if (filter.filteringJobs)
                query = query.whereGreaterThanOrEqualTo("salary", it)
            else
                query = query.whereLessThanOrEqualTo("salary", it)
        }

        return query
    }

    override fun locationFilter(item: Job): Boolean {
        // if filter is not set, don't filter the job
        val filter = jobFilter ?: return true

        with(filter) {
            if (filteringJobs != item.itIsJob) return false
            uid?.let {
                if (item.uid != it)
                    return false
            }

            val filterSalary = salary
            val jobSalary: Float? = item.salary

            if (filterSalary != null && jobSalary != null) {
                if (filteringJobs && jobSalary < filterSalary) { // remove jobs that pay less than requested
                    return false
                }
                if (!filteringJobs && jobSalary > filterSalary) // remove proposals that asks for more money that requested
                    return false
            }
        }
        return true
    }

    fun removeFilter() {
        jobFilter = null
    }

    // add filter properties here
    data class JobFilter(
        var uid: String? = null,
        var range: Int = MAX_RANGE_KM,
        var query: String = "",
        var location: Location? = null,
        var filteringJobs: Boolean = true, // used to load jobs or proposal
        var salary: Float? = null
    )
}
