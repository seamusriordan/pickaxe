package services

import java.io.InputStreamReader
import java.net.HttpURLConnection

class NflService() {
    lateinit var tokenEndpoint: HttpURLConnection
    private var _accessToken: String? = null

    var accessToken: String
        get() {
            if(this._accessToken != null){
                return _accessToken as String
            }

            val stream = tokenEndpoint.inputStream

            val reader = InputStreamReader(stream)

            return reader.readText()
        }
        set(value) {_accessToken = value}
}