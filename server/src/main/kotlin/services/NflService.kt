package services

import java.io.InputStreamReader
import java.net.HttpURLConnection

class NflService(private val urlConnection: HttpURLConnection) {
    fun getAccessToken(): String {
        val stream = urlConnection.inputStream

        val reader = InputStreamReader(stream)

        return reader.readText()
    }
}