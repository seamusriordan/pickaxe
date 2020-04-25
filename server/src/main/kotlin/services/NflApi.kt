package services

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import dto.GameDTO
import dto.WeekDTO
import dto.nfl.week.Node
import dto.nfl.week.QueryDTO
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

class NflApi(private val tokenURL: URL, private val apiURL: URL) {
    private var _accessToken: String? = null
    var now = { Date() }

    var accessToken: String
        get() {
            if (!tokenIsValid(_accessToken)) {
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
        connection.let {
            it.requestMethod = "POST"
            it.doOutput = true
            setCommonHeaders(it)
            it.setRequestProperty("x-domain-id", "100")
        }

        val dataOutputStream = DataOutputStream(connection.outputStream)
        dataOutputStream.writeBytes("grant_type=client_credentials")
        dataOutputStream.close()

        val stream = connection.inputStream
        val response = InputStreamReader(stream).readText()
        return responseMap(response)["access_token"] as String
    }

    private fun responseMap(response: String) = ObjectMapper().readValue(response, HashMap::class.java)

    fun getWeeks(): List<WeekDTO> {
        return ArrayList<WeekDTO>(0)
    }

    fun getWeek(week: WeekDTO): List<GameDTO> {
        val result = ArrayList<GameDTO>(0)

        val stream = createWeekQueryConnection(week).inputStream
        val response = ObjectMapper().readValue(InputStreamReader(stream).readText(), QueryDTO::class.java)

        for (edge in response.data.viewer.league.games.edges) {
            result.add(GameDTO(formatGameName(edge.node), week.name))
        }

        return result
    }

    private fun createWeekQueryConnection(week: WeekDTO): HttpURLConnection {
        val season = 2019

        val fullApiUrl = URL(
            apiURL,
            "/v3/shield/?query=query%7Bviewer%7Bleague%7Bgames(first%3A100%2Cweek_seasonValue%3A${season}%2C"
                    + "week_seasonType%3A${week.weekType}%2Cweek_weekValue%3A${week.week}%2C)%7Bedges%7Bnode%7Bid%20"
                    + "awayTeam%7BnickName%20abbreviation%20%7DhomeTeam%7BnickName%20id%20abbreviation%20"
                    + "%7D%7D%7D%7D%7D%7D%7D&variables=null"
        )

        val connection = fullApiUrl.openConnection() as HttpURLConnection
        connection.let {
            setCommonHeaders(it)
            it.setRequestProperty("authorization", "Bearer $accessToken")
            it.setRequestProperty("accept", "*/*")
            it.setRequestProperty("Content-Type", "application/json")
        }
        return connection
    }

    private fun formatGameName(game: Node) = game.awayTeam.abbreviation + "@" + game.homeTeam.abbreviation

    private fun setCommonHeaders(connection: HttpURLConnection) {
        connection.setRequestProperty("authority", "api.nfl.com")
        connection.setRequestProperty("origin", "https://www.nfl.com")
        connection.setRequestProperty("referer", "https://www.nfl.com/")
        connection.setRequestProperty(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
        )
    }
}
