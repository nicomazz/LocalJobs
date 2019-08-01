package com.esp.localjobs.data.base

import com.esp.localjobs.data.models.User

interface IUserRepository {
    fun addUser(u: User)
    suspend fun getUserDetails(id: String): User?
}