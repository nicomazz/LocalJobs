package com.esp.localjobs.data.repository

import com.esp.localjobs.data.base.FirebaseDatabaseRepository
import com.esp.localjobs.data.base.Methods
import com.esp.localjobs.data.models.Job
import java.lang.Exception

class JobsRepository : FirebaseDatabaseRepository<Job>(), Methods<Job> {
    private val jobsCollection = db.collection(getRootNode())
    override fun getRootNode() = "jobs"

    override fun add(item: Job, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        jobsCollection.document()
            .set(item)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener{ it() } }
                onFailure?.let { task.addOnFailureListener{ error -> it(error) } }
            }
    }

    override fun delete(id: String, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        jobsCollection.document(id)
            .delete()
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener{ it() } }
                onFailure?.let { task.addOnFailureListener{ exception ->  it(exception) } }
            }
    }

    override fun update(id: String, newItem: Job, onSuccess: (() -> Unit)?, onFailure: ((e: Exception) -> Unit)?) {
        jobsCollection.document(id)
            .set(newItem)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener{ it() } }
                onFailure?.let { task.addOnFailureListener{ exception -> it(exception) } }
            }
    }

    override fun update(
        id: String,
        oldItem: Job,
        newItem: Job,
        onSuccess: (() -> Unit)?,
        onFailure: ((e: Exception) -> Unit)?
    ) {
        if (oldItem == newItem)
            return

        val updates = HashMap<String, Any?>()
        // update only different fields
        Job::class.java.declaredFields.forEach {
            // isAccessible check it field is private
            if (it.isAccessible && it.get(oldItem) != it.get(newItem))
                updates[it.name] = it.get(newItem)
        }

        jobsCollection.document(id)
            .update(updates)
            .also { task ->
                onSuccess?.let { task.addOnSuccessListener{ it() } }
                onFailure?.let { task.addOnFailureListener{ exception -> it(exception) } }
            }
    }

}
