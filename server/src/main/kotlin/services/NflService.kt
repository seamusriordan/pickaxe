package services

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStreamReader
import java.net.URL

class NflService(private val tokenURL: URL) {
    private var _accessToken: String? = null

    var accessToken: String
        get() {
            if(this._accessToken != null){
                return _accessToken as String
            }
            return fetchNewToken()
        }
        set(token) {_accessToken = token}

    private fun fetchNewToken(): String {
        val tokenEndpoint = tokenURL.openConnection()
        val stream = tokenEndpoint.inputStream
        val response = InputStreamReader(stream).readText()

        return responseMap(response)["access_token"] as String
    }

    private fun responseMap(response: String) = ObjectMapper().readValue(response, HashMap::class.java)
}