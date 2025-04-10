package kurd.reco.core.viewmodels

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kurd.reco.core.ResourceState
import kurd.reco.core.SGCheck
import kurd.reco.core.api.Api.API_URL
import kurd.reco.core.api.Api.CORS_PROXY
import kurd.reco.core.api.Api.ROXIO_API
import kurd.reco.core.api.Resource
import kurd.reco.core.api.app
import kurd.reco.core.api.localApp
import kurd.reco.core.data.AuthLoginResponse
import kurd.reco.core.data.AuthTokenResponse

class AuthVM: ViewModel() {
    var loginState = ResourceState<String>(Resource.Loading)
    var accessToken = ResourceState<String>(Resource.Loading)
    private lateinit var appSignature: String

    fun getAndroidID(context: Context): String {
        appSignature = SGCheck.getSG(context)
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
                val response = localApp.post("$ROXIO_API/auth/login", mapOf("app-md" to appSignature), json = jsonData).parsed<AuthLoginResponse>()
                if (response.rememberToken != null) {
                    loginState.setSuccess(response.rememberToken)
                } else {
                    loginState.setFailure(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loginState.setFailure(e.localizedMessage ?: e.message ?: "Unknown error")
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
                val response = localApp.post("$ROXIO_API/auth/token", mapOf("app-md" to appSignature), json = jsonData).parsed<AuthTokenResponse>()
                if (response.accessToken != null) {
                    accessToken.setSuccess(response.accessToken)
                } else {
                    accessToken.setFailure(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                accessToken.setFailure(e.localizedMessage ?: e.message ?: "Unknown error")
            }
        }
    }

    fun resetLoginState() = loginState.setLoading()
}