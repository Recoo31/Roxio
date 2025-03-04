package kurd.reco.core

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kurd.reco.core.api.Api.API_URL
import kurd.reco.core.api.Api.CORS_PROXY
import kurd.reco.core.api.Resource
import kurd.reco.core.api.app
import kurd.reco.core.data.AuthLoginResponse
import kurd.reco.core.data.AuthTokenResponse

class AuthVM: ViewModel() {
    var loginState = ResourceState<String>(Resource.Loading)
    var accessToken = ResourceState<String>(Resource.Loading)

    fun getAndroidID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun login(username: String, password: String, hwid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonData = mapOf(
                "username" to username.replace("\t", ""),
                "password" to password.replace("\t", ""),
                "hwid" to hwid
            )

            try {
                val response = app.post("$CORS_PROXY/$API_URL/auth/login", json = jsonData).parsed<AuthLoginResponse>()
                if (response.rememberToken != null) {
                    loginState.setSuccess(response.rememberToken)
                } else {
                    loginState.setFailure(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getToken(rememberToken: String, hwid: String) {
        accessToken.setLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val jsonData = mapOf(
                "remember_token" to rememberToken,
                "hwid" to hwid
            )

            try {
                val response = app.post("$CORS_PROXY/$API_URL/auth/token", json = jsonData).parsed<AuthTokenResponse>()
                if (response.accessToken != null) {
                    accessToken.setSuccess(response.accessToken)
                } else {
                    accessToken.setFailure(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                accessToken.setFailure("Failed to get token")
            }
        }
    }

    fun resetLoginState() = loginState.setLoading()
}