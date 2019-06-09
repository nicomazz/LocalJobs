package com.esp.localjobs.data.base

import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class BaseValueEventListener<Model>(
    private val callback: BaseRepository.RepositoryCallback<Model>,
    private val clazz: Class<Model>
) : EventListener<QuerySnapshot> {
    override fun onEvent(results: QuerySnapshot?, e: FirebaseFirestoreException?) {
        e?.let {
            callback.onError(it)
            return
        }

        val list = ArrayList<Model>().also { ll ->
            results?.documents?.forEach {
                try {
                    ll.add(it.toObject(clazz)!!)
                } catch (e: Exception) {
                    Log.e("valueEventListener", "problem in casting! ${e.message}")
                }
            }
        }

        callback.onSuccess(list)
    }
}
