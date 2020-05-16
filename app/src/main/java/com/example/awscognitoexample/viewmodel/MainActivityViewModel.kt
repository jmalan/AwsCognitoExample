package com.example.awscognitoexample.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobileconnectors.cognitoauth.Auth
import com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession
import com.amazonaws.mobileconnectors.cognitoauth.handlers.AuthHandler
import com.amazonaws.mobileconnectors.cognitoauth.util.JWTParser
import com.example.awscognitoexample.R
import org.json.JSONObject
import java.lang.Exception
import java.lang.ref.WeakReference

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val TAG = MainActivityViewModel::class.java.simpleName
    }

    private var auth: Auth? = null
    private val appRedirect = Uri.parse(getApplication<Application>().getString(R.string.app_redirect))

    val signedUserName = MutableLiveData<String?>()
    val signedUserEmail = MutableLiveData<String?>()
    val signed = MutableLiveData<Boolean>()

    init {
        signed.value = false

        getApplication<Application>().let {
            auth = Auth.Builder()
                .setAppClientId(it.getString(R.string.cognito_client_id))
                .setAppCognitoWebDomain(it.getString(R.string.cognito_web_domain))
                .setApplicationContext(it)
                .setAuthHandler(AuthCallback(WeakReference(this)))
                .setSignInRedirect(it.getString(R.string.app_redirect))
                .setSignOutRedirect(it.getString(R.string.app_redirect))
                .build()
        }
    }

    fun signInCognitoUser() {
        auth?.getSession()
    }

    fun signInGoogleUser() {
        getApplication<Application>().let {
            auth = Auth.Builder()
                    .setAppClientId(it.getString(R.string.cognito_client_id))
                    .setAppCognitoWebDomain(it.getString(R.string.cognito_web_domain))
                    .setIdentityProvider("Google")
                    .setApplicationContext(it)
                    .setAuthHandler(AuthCallback(WeakReference(this)))
                    .setSignInRedirect(it.getString(R.string.app_redirect))
                    .setSignOutRedirect(it.getString(R.string.app_redirect))
                .build()
            auth?.getSession()
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    fun redirectIntentUri(intentUri: Uri) {
        if (appRedirect.host.equals(intentUri.host)) {
            auth?.getTokens(intentUri)
        }
    }

    class AuthCallback(private val viewModel: WeakReference<MainActivityViewModel>) : AuthHandler {
        override fun onSuccess(session: AuthUserSession?) {
            session?.let {
                Log.d(TAG, "Received access token: ${it.accessToken.jwtToken} identity token: ${it.idToken.jwtToken}")
                val jsonObject = JWTParser.getPayload(it.idToken.jwtToken)
                viewModel.get()?.let { viewModel ->
                    viewModel.signedUserName.value = jsonObject.optString("name")
                    viewModel.signedUserEmail.value = jsonObject.optString("email")
                    viewModel.signed.value = true
                }
            }
        }

        override fun onFailure(e: Exception?) {
            Log.d(TAG, "Failed to auth")
            viewModel.get()?.let {
                it.signed.value = false
                it.signedUserName.value = null
                it.signedUserEmail.value = null
            }
        }

        override fun onSignout() {
            Log.d(TAG, "Signed out")
            viewModel.get()?.let {
                it.signed.value = false
                it.signedUserName.value = null
                it.signedUserEmail.value = null
            }
        }

    }
}