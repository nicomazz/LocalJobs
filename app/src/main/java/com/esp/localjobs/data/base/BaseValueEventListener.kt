package com.esp.localjobs.data.base

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.lang.reflect.ParameterizedType


class BaseValueEventListener<Model>(
    private val callback: FirebaseDatabaseRepository.FirebaseDatabaseRepositoryCallback<Model>,
    private val clazz : Class<Model>
) : EventListener<QuerySnapshot> {
    override fun onEvent(results: QuerySnapshot?, e: FirebaseFirestoreException?) {

        e?.let {
            callback.onError(it)
            return
        }


       // val typeClass = (javaClass
        //    .genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<Model>;
        val list = ArrayList<Model>().also { ll ->
            results?.documents?.forEach {
                try {
                    ll.add(it.toObject(clazz)!!)
                } catch (e: TypeCastException) {
                    Log.e("valueEventListener", "problem in casting!")
                }
            }
        }

        callback.onSuccess(list)
    }
    inline fun <reified R: Model> f(obj : DocumentSnapshot): R{
        return obj.toObject(R::class.java)!!

    }

    /* @Suppress("UNCHECKED_CAST")
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
     }*/
}

inline fun <reified T : Any> foo() = T::class.java
