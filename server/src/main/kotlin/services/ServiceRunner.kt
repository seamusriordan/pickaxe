package services

import getEnvOrDefault
import java.net.URL

class ServiceRunner {
    fun start() {
        val nflApiRoot = getEnvOrDefault("NFL_API_ROOT", "http://nfl-wiremock:8080")
        NflApi(URL("${nflApiRoot}/v1/reroute"), URL(nflApiRoot)).accessToken
    }

}
