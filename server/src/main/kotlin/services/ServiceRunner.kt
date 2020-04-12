package services

import java.net.URL

class ServiceRunner {
    fun start() {
        val nflApi = NflApi(URL("https://api.nfl.com/v1/reroute"))
        println(nflApi.accessToken)
    }

}
