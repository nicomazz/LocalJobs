package com.esp.localjobs.data.base

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class BaseValueEventListener<Model>(
    private val callback: FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Model>
) : ValueEventListener {

    @Suppress("UNCHECKED_CAST")
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val list = ArrayList<Model>().apply {
            dataSnapshot.children.forEach {
                try {
                    this += it.value as Model
                } catch (e : TypeCastException){
                    Log.e("valueEventListener","problem in casting!")
                }
            }
        }

        callback.onSuccess(list)
    }

    override fun onCancelled(databaseError: DatabaseError) {
        callback.onError(databaseError.toException())
    }
}