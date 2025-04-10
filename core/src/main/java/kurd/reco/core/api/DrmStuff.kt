package kurd.reco.core.api

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import kurd.reco.core.api.Api.API_URL
import kurd.reco.core.api.Api.CORS_PROXY
import kurd.reco.core.api.Api.ROXIO_API
import kurd.reco.core.api.model.DrmDataModel
import kurd.reco.core.api.model.PlayDataModel
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

object DrmStuff {
    suspend fun getDrmKeys(playData: PlayDataModel): String? {
        //if (!status) return null

        val url = playData.urls[0].second
        val responsePSSH = app.get(url, headers = playData.streamHeaders!!).text

        val pssh = parsePSSH(responsePSSH) ?: return null

        return getCacheDrm(pssh) ?: sendToCDRM(pssh, playData.drm!!)
    }

    suspend fun getDrmKeys(response: String, drmData: DrmDataModel): String? {
        //if (!status) return null

        val pssh = parsePSSH(response) ?: return null

        return getCacheDrm(pssh) ?: sendToCDRM(pssh, drmData)
    }

    private fun parsePSSH(body: String): String? {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(body.reader())

        var eventType = parser.eventType
        var pssh: String? = null

        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "ContentProtection" && parser.getAttributeValue(null, "schemeIdUri") == "urn:uuid:EDEF8BA9-79D6-4ACE-A3C8-27DCD51D21ED") {
                        eventType = parser.next()
                        while (eventType != XmlPullParser.END_TAG) {
                            if (eventType == XmlPullParser.START_TAG && parser.name == "cenc:pssh") {
                                pssh = parser.nextText()
                                break
                            }
                            eventType = parser.next()
                        }
                        break
                    }
                }
                // Move to the next event
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (pssh != null) {
            println("Parsed Widevine PSSH: $pssh")
            return pssh
        } else {
            println("Widevine PSSH not found.")
            return null
        }
    }

    private suspend fun sendToCDRM(pssh: String, drmData: DrmDataModel): String? {
        val api = "$ROXIO_API/cdn/"

        val jsonData = mapOf(
            "PSSH" to pssh,
            "License URL" to drmData.licenseUrl,
            "Headers" to formatHeaders(drmData.headers),
            "JSON" to "{}",
            "Cookies" to "{}",
            "Data" to "{}",
            "Proxy" to ""
        )

        val response = localApp.post(api, json = jsonData)

        return try {
            val message = response.parsed<CDRMResponse>().message.replace("\n", "")
            message
        } catch (t: Throwable) {
            null
        }
    }

    private suspend fun getCacheDrm(pssh: String): String? {
        val url = "$ROXIO_API/cdn/cache"
        val jsonData = mapOf(
            "PSSH" to pssh
        )

        return try {
            val response = localApp.post(url, json = jsonData, timeout = 2000L)
            val message = response.parsed<CDRMResponse>().message
            if (message == "Not found") return null
            message.replace("\n", "")
        } catch (t: Throwable) {
            null
        }
    }

    private fun formatHeaders(headers: Map<String, String>?): String {
        return headers?.entries?.joinToString(prefix = "{", postfix = "}") {
            "\"${it.key}\": \"${it.value}\""
        } ?: "{}" // Return an empty JSON object if headers are null
    }

    @Keep
    data class CDRMResponse(
        @JsonProperty("Message")
        val message: String,
    )

}