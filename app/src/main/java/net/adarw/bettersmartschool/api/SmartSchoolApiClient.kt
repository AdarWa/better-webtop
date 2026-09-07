package net.adarw.bettersmartschool.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SmartSchoolApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    suspend fun getScheduleData(requestData: ShotefScheduleRequest, webToken: String, uniqueId: String): ShotefScheduleResponse {
        Log.e("NetworkClient", "Fetched from network!")
        val response = client.post("https://webtopserver.smartschool.co.il/server/api/shotef/ShotefSchedualeData") {
            contentType(ContentType.Application.Json)

            header("Language", "he")
            header("Rememberme", "0")
            header("X-Xsrf-Token", "")

            // Standard headers expected by the server
            header(HttpHeaders.Origin, "https://webtop.smartschool.co.il")
            header(HttpHeaders.Referrer, "https://webtop.smartschool.co.il/")
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36")

            val cookieString = "uniqueId=$uniqueId; allowCookies=1; IsBio=false; input=0; webToken=$webToken"
            header(HttpHeaders.Cookie, cookieString)

            setBody(requestData)
        }
        return response.body()
    }

    fun close() {
        client.close()
    }
}