package kurd.reco.core.data

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthLoginResponse(
    @JsonProperty("remember_token")
    val rememberToken: String?,
    val message: String?
)

data class AuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String?,
    val message: String?
)