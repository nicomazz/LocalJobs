package com.esp.localjobs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, // Initial state, the user needs to authenticate
        UNAUTHENTICATED, // The user has authenticated successfully
        INVALID_AUTHENTICATION // Authentication failed
    }

    val authenticationState = MutableLiveData<AuthenticationState>()

    init {
        // In this example, the user is always unauthenticated when MainActivity is launched
        authenticationState.value = if (FirebaseAuth.getInstance().currentUser != null)
            AuthenticationState.AUTHENTICATED
        else AuthenticationState.UNAUTHENTICATED
    }

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }

    fun refuseAuthentication() {
        authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }
}