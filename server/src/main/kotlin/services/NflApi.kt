package services

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

class NflApi(private val tokenURL: URL) {
    private var _accessToken: String? = null
    var now = { Date() }

    var accessToken: String
        get() {
            if(!tokenIsValid(_accessToken)){
                _accessToken = fetchNewToken()
            }
            return _accessToken!!
        }
        set(token) {
            _accessToken = token
        }

    private fun tokenIsValid(token: String?): Boolean {
        return token != null && now() < JWT.decode(token).expiresAt
    }

    private fun fetchNewToken(): String {
        val connection = tokenURL.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        val dataOutputStream = DataOutputStream(connection.outputStream)
        dataOutputStream.writeBytes("grant_type=client_credentials")
        dataOutputStream.close()

        val stream = connection.inputStream
        val response = InputStreamReader(stream).readText()
        return responseMap(response)["access_token"] as String
    }

    private fun responseMap(response: String) = ObjectMapper().readValue(response, HashMap::class.java)
}
