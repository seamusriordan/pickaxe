package services

import java.io.InputStreamReader
import java.net.URL

class NflService(private val tokenURL: URL) {
    private var _accessToken: String? = null

    var accessToken: String
        get() {
            if(this._accessToken != null){
                return _accessToken as String
            }

            val tokenEndpoint = tokenURL.openConnection()
            val stream = tokenEndpoint.inputStream
            val reader = InputStreamReader(stream)
            return reader.readText()
        }
        set(token) {_accessToken = token}
}