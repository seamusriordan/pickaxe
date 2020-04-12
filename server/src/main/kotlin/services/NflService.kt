package services

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

class NflService(private val tokenURL: URL) {
    private var _accessToken: String? = null
    var now = { Date() }

    var accessToken: String
        get() {
            if (tokenIsValid(_accessToken)) {
                return _accessToken as String
            }
            return fetchNewToken()
        }
        set(token) {
            _accessToken = token
        }

    private fun tokenIsValid(token: String?): Boolean {
        if (token == null || JWT.decode(token).expiresAt < now()) {
            return false
        }
        return true
    }

    private fun fetchNewToken(): String {
        val stream = tokenURL.openConnection().inputStream
        val response = InputStreamReader(stream).readText()
        return responseMap(response)["access_token"] as String
    }

    private fun responseMap(response: String) = ObjectMapper().readValue(response, HashMap::class.java)
}
