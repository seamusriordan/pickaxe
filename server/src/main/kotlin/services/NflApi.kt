package services

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import dto.GameDTO
import dto.WeekDTO
import dto.nfl.api.game.Details
import dto.nfl.api.game.GameQueryDTO
import dto.nfl.api.week.Edge
import dto.nfl.api.week.Node
import dto.nfl.api.week.WeekQueryDTO
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.HashMap

class NflApi(private val tokenURL: URL, private val apiURL: URL) {
    private var _accessToken: String? = null
    var now = { Date() }

    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'")

    init {
        formatter.timeZone = TimeZone.getTimeZone("UTC")
    }

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
        val response = ObjectMapper().readValue(InputStreamReader(stream).readText(), WeekQueryDTO::class.java)

        for (edge in response.data.viewer.league.games.edges) {
            val game = buildGameInWeek(edge, week)
            result.add(game)
        }

        return result
    }

    fun getGame(game: GameDTO): GameDTO {
        val stream = createGameQueryConnection(game.id!!).inputStream
        val responseText = InputStreamReader(stream).readText()
        val response = ObjectMapper().readValue(responseText, GameQueryDTO::class.java)
        val details = response.data.viewer.gameDetailsByIds.first()

        return buildGameResponse(game, details)
    }

    private fun buildGameInWeek(edge: Edge, week: WeekDTO): GameDTO {
        return GameDTO(formatGameName(edge.node), week.name).apply {
            id = edge.node.gameDetailId
            gameTime = OffsetDateTime.parse(edge.node.gameTime)
        }
    }

    private fun buildGameResponse(game: GameDTO, details: Details): GameDTO {
        return GameDTO(game.name, game.week).apply {
            if (details.phase.contains("FINAL")) {
                result = determineOutcome(details)
            }
            id = game.id
            gameTime = game.gameTime
        }
    }

    private fun determineOutcome(details: Details): String {
        var result = "TIE"
        if (details.homePointsTotal > details.visitorPointsTotal) {
            result = details.homeTeam.abbreviation
        }
        if (details.homePointsTotal < details.visitorPointsTotal) {
            result = details.visitorTeam.abbreviation
        }
        return result
    }

    private fun createWeekQueryConnection(week: WeekDTO): HttpURLConnection {
        val season = 2019

        val fullApiUrl = URL(
            apiURL,
            "/v3/shield/?query=query%7Bviewer%7Bleague%7Bgames(first%3A100%2Cweek_seasonValue%3A${season}%2C"
                    + "week_seasonType%3A${week.weekType}%2Cweek_weekValue%3A${week.week}%2C)%7Bedges%7Bnode%7B"
                    +"gameDetailId%20gameTime%20awayTeam%7BnickName%20abbreviation%20%7DhomeTeam%7BnickName%20abbreviation%20"
                    + "%7D%7D%7D%7D%7D%7D%7D&variables=null"
        )

        return connectionWithQueryHeaders(fullApiUrl)
    }

    private fun createGameQueryConnection(id: UUID): HttpURLConnection {
        val fullUrl =
            URL(
                apiURL,
                "/v3/shield/?query=query%7Bviewer%7BgameDetailsByIds(ids%3A%5B%22$id%22%2C%5D)%7Bid%2Cphase%2ChomePointsTotal%2CvisitorPointsTotal%2Cphase%2ChomeTeam%7Babbreviation%7D%2CvisitorTeam%7Babbreviation%7D%7D%7D%7D&variables=null\n"
            )

        return connectionWithQueryHeaders(fullUrl)

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

    private fun connectionWithQueryHeaders(fullUrl: URL): HttpURLConnection {
        val connection = fullUrl.openConnection() as HttpURLConnection
        connection.let {
            setCommonHeaders(it)
            it.setRequestProperty("authorization", "Bearer $accessToken")
            it.setRequestProperty("accept", "*/*")
            it.setRequestProperty("Content-Type", "application/json")
        }
        return connection
    }
}


