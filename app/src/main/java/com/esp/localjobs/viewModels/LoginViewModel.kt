package com.esp.localjobs.viewModels

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.localjobs.data.models.User
import com.esp.localjobs.data.models.toUser
import com.esp.localjobs.data.repository.userFirebaseRepository
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainScope

@UseExperimental(InternalCoroutinesApi::class)
class LoginViewModel : ViewModel(), CoroutineScope by MainScope() {

    enum class AuthenticationState {
        AUTHENTICATED, // Initial state, the user needs to authenticate
        UNAUTHENTICATED, // The user has authenticated successfully
        INVALID_AUTHENTICATION // Authentication failed
    }

    val authenticationState = MutableLiveData<AuthenticationState>()
    private val RC_SIGN_IN: Int = 43
    private val TAG = "LoginViewModel"

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

    fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun getUserName(): String? = FirebaseAuth.getInstance().currentUser?.displayName

    fun getUserProfilePicture(): String? = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()

    fun authenticate(loginFragment: Fragment) {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        loginFragment.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
             val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                authenticationState.value = AuthenticationState.AUTHENTICATED
                storeNameAndImage()
            } else {
                Log.e(TAG, "Error in authentication: ${response?.error?.errorCode}")
                authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    fun getCurrentUser(): User? = FirebaseAuth.getInstance().currentUser?.toUser()

    private fun storeNameAndImage() {
        FirebaseAuth.getInstance().currentUser?.toUser()?.let {
            userFirebaseRepository.addUser(it)
        }
    }
}
