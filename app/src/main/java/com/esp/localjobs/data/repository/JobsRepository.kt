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
        // firebase doesn't allow to filter using a "contains", so filter by query is not implemented here
        val filter = jobFilter ?: return null

        var query = collection.whereEqualTo("itIsJob", filter.filteringJobs)
        filter.uid?.let {
            query = query.whereEqualTo("uid", it)
        }

        filter.salary?.let {
            query = if (filter.filteringJobs)
                query.whereGreaterThanOrEqualTo("salary", it)
            else
                query.whereLessThanOrEqualTo("salary", it)
        }

        return query
    }

    override fun filter(item: Job): Boolean {
        // if filter is not set, don't filter the job
        val filter = jobFilter ?: return true

        with(filter) {
            if (filteringJobs != item.itIsJob) return false
            uid?.let {
                val jobHasUIDRequested = item.uid == it
                if (!jobHasUIDRequested) return false
            }

            // get jobs with minimum / maximum salary
            val filterSalary = salary
            val jobSalary: Float? = item.salary?.toFloatOrNull()

            val filterRequiresSalaryButIsNull = filterSalary != null && jobSalary == null
            if (filterRequiresSalaryButIsNull) return false

            if (filterSalary != null && jobSalary != null) {
                val filteringJobsAndSalaryIsNotEnough = filteringJobs && (jobSalary < filterSalary)
                if (filteringJobsAndSalaryIsNotEnough) {
                    return false
                }

                val filteringProposalsAndSalaryIsTooBig = !filteringJobs && (jobSalary > filterSalary)
                if (filteringProposalsAndSalaryIsTooBig)
                    return false
            }

            query?.let {
                val itemTitle = item.title
                if (itemTitle != null && !itemTitle.contains(it, true))
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
        var query: String? = null,
        var location: Location? = null,
        var filteringJobs: Boolean = true, // used to load jobs or proposal
        var salary: Float? = null
    )
}
