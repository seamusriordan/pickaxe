package services

import com.auth0.jwt.JWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import dto.GameDTO
import dto.WeekDTO
import dto.nfl.api.game.*
import dto.nfl.api.week.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.*
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
import kotlin.collections.ArrayList


class NflApiTest {
    private val handler = MockURLStreamHandler
    private val tokenURL = URL("https://tokenendpoint")
    private val baseApiUrl = URL("http://apiuri")
    private val mockTokenConnection = mockkClass(HttpURLConnection::class)
    private val mockApiConnection = mockkClass(HttpURLConnection::class)

    private val season = 2019

    private val absoluteTime = { GregorianCalendar(season, 3, 12, 13, 5).time }

    private val requestOutputStream = ByteArrayOutputStream()

    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'")


    @BeforeEach
    fun beforeEach() {
        formatter.timeZone = TimeZone.getTimeZone("UTC")

        handler.setConnection(tokenURL, mockTokenConnection)
        every { mockTokenConnection.requestMethod = "POST" } returns Unit
        every { mockTokenConnection.outputStream } returns requestOutputStream
        every { mockTokenConnection.doOutput = true } returns Unit
        every { mockTokenConnection.setRequestProperty(any(), any()) } returns Unit
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse("default-token")
        every { mockApiConnection.setRequestProperty(any(), any()) } returns Unit
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfToken() {
        val expectedToken = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldMakePOSTToGetToken() {
        val expectedToken = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        nflService.accessToken

        verify(exactly = 1) { mockTokenConnection.requestMethod = "POST" }
    }

    @Test
    fun outputStreamPOSTsWithGrantTypeBody() {
        val expectedBody = "grant_type=client_credentials"

        val expectedToken = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        nflService.accessToken

        assertArrayEquals(expectedBody.toByteArray(), requestOutputStream.toByteArray())
    }


    @Test
    fun connectionHasCorrectPropertiesSet() {
        val expectedToken = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        nflService.accessToken

        val properties = ArrayList<String>(5).apply {
            add("authority")
            add("origin")
            add("x-domain-id")
            add("referer")
            add("user-agent")
        }

        properties.map { property ->
            verify { mockTokenConnection.setRequestProperty(property, any()) }
        }
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfLongToken() {
        val expectedToken = generateExpiringToken(2)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun ifAccessTokenIsSetAndValidDoNotFetchNewToken() {
        val expectedToken = generateExpiringToken(1)
        val nflService = nflServiceWithFixedTime(tokenURL, expectedToken)
        val unexpectedToken = generateExpiringToken(2)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(unexpectedToken)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun gettingTokenTwiceWillOnlyFetchOnce() {
        val nflService = nflServiceWithFixedTime(tokenURL)
        val expectedToken = generateExpiringToken(1)
        val unexpectedToken = generateExpiringToken(2)

        every {
            mockTokenConnection.inputStream
        } returnsMany listOf(
            buildByteStreamResponse(expectedToken),
            buildByteStreamResponse(unexpectedToken)
        )


        nflService.accessToken
        val secondToken = nflService.accessToken

        assertEquals(expectedToken, secondToken)
        verify(exactly = 1) { mockTokenConnection.inputStream }
    }

    @Test
    fun ifAccessTokenIsSetAndInvalidFetchNewToken() {
        val expiredToken = generateExpiringToken(-1)
        val nflService = nflServiceWithFixedTime(tokenURL, expiredToken)
        val expectedToken = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(expectedToken)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun getsWeeksFromDatabaseWithOneWeek() {
        val expectedWeeks = ArrayList<WeekDTO>(0)

        val nflService = NflApi(URL("http://url"), baseApiUrl)

        val weeks = nflService.getWeeks()

        assertEquals(expectedWeeks, weeks)
    }

    @Test
    fun getWeekGetsGamesFromNFLWithOneGameRegularWeek5() {
        val weekTypeQuery = "REG"
        val weekQuery = 5
        val weekName = "Week 5"
        val week = WeekDTO(weekName).apply {
            weekType = weekTypeQuery
            week = weekQuery
        }
        val uri = buildRelativeApiWeekQueryUrl(season, weekTypeQuery, weekQuery)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val expectedGames = baseWeekQueryDTO.apply {
            data.viewer.league.games.edges = listOf(
                buildGame("ARI", "SF", defaultGameStart)
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames)
            .byteInputStream()

        val result = NflApi(tokenURL, baseApiUrl).getWeek(week)

        verify(exactly = 1) { mockApiConnection.inputStream }
        assertEquals(1, result.size)
        assertEquals("ARI@SF", result.first().name)
        assertEquals(weekName, result.first().week)
        assertEquals(expectedGames.data.viewer.league.games.edges.first().node.gameDetailId, result.first().id)
        assertEquals(defaultGameStart, result.first().gameTime)
    }

    @Test
    fun getWeekGetsGamesFromNFLWithOneGamePreseasonWeek1() {
        val weekTypeQuery = "PRE"
        val weekQuery = 1
        val weekName = "Preseason Week 1"
        val week = WeekDTO(weekName).apply {
            weekType = weekTypeQuery
            week = weekQuery
        }
        val uri = buildRelativeApiWeekQueryUrl(season, weekTypeQuery, weekQuery)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        every { mockApiConnection.setRequestProperty(any(), any()) } returns Unit
        val expectedGames = baseWeekQueryDTO.apply {
            data.viewer.league.games.edges = listOf(
                buildGame("ARI", "SF", defaultGameStart)
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames)
            .byteInputStream()

        val result = NflApi(tokenURL, baseApiUrl).getWeek(week)

        verify(exactly = 1) { mockApiConnection.inputStream }
        assertEquals(1, result.size)
        assertEquals(weekName, result.first().week)
    }

    @Test
    fun getWeekWithDifferentBaseUrl() {
        val weekTypeQuery = "REG"
        val weekQuery = 5
        val weekName = "Week 5"
        val uri = buildRelativeApiWeekQueryUrl(season, weekTypeQuery, weekQuery)
        val week = WeekDTO(weekName).apply {
            weekType = weekTypeQuery
            week = weekQuery
        }
        val differentBaseUrl = URL("https://api.nfl.com")
        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(differentBaseUrl, uri), mockConnection)
        every { mockConnection.setRequestProperty(any(), any()) } returns Unit
        val expectedGames = baseWeekQueryDTO.apply {
            data.viewer.league.games.edges = listOf(
                buildGame("ARI", "SF", defaultGameStart)
            )
        }
        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream()

        val result = NflApi(tokenURL, differentBaseUrl).getWeek(week)

        verify(exactly = 1) { mockConnection.inputStream }
        assertEquals(1, result.size)
        assertEquals("ARI@SF", result.first().name)
        assertEquals(weekName, result.first().week)
    }

    @Test
    fun getWeekGetsGamesFromNFLWithTwoGamesRegularWeek8() {
        val weekName = "Week 8"
        val weekTypeQuery = "REG"
        val weekQuery = 8
        val uri = buildRelativeApiWeekQueryUrl(season, weekTypeQuery, weekQuery)
        val week = WeekDTO(weekName).apply {
            weekType = weekTypeQuery
            week = weekQuery
        }
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val expectedGames = baseWeekQueryDTO.apply {
            data.viewer.league.games.edges = listOf(
                buildGame("CHI", "IND", defaultGameStart),
                buildGame("TEN", "MIA", defaultGameStart.plusHours(3))
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames)
            .byteInputStream()

        val result = NflApi(tokenURL, baseApiUrl).getWeek(week)

        verify(exactly = 1) { mockApiConnection.inputStream }
        assertEquals(2, result.size)
        assertEquals("CHI@IND", result[0].name)
        assertEquals(weekName, result[0].week)
        assertEquals("TEN@MIA", result[1].name)
        assertEquals(weekName, result[1].week)
        assertEquals(defaultGameStart.plusHours(3), result[1].gameTime)
    }

    @Test
    fun weekRequestHasStaticHeadersSet() {
        val uri = buildRelativeApiWeekQueryUrl(season, "REG", 3)
        val week = WeekDTO("Week 3").apply {
            weekType = "REG"
            week = 3
        }
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        every { mockApiConnection.inputStream } returns
                ObjectMapper().writeValueAsString(baseWeekQueryDTO).byteInputStream()

        NflApi(tokenURL, baseApiUrl).getWeek(week)

        val properties = ArrayList<String>(5).apply {
            add("authority")
            add("origin")
            add("accept")
            add("referer")
            add("user-agent")
            add("Content-Type")
        }
        properties.map { property ->
            verify { mockApiConnection.setRequestProperty(property, any()) }
        }
    }

    @Test
    fun weekRequestHasBearerTokenSet() {
        val uri = buildRelativeApiWeekQueryUrl(season, "REG", 3)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val token = generateExpiringToken(1)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(token)
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(baseWeekQueryDTO)
            .byteInputStream()
        val week = WeekDTO("Week 3").apply {
            weekType = "REG"
            week = 3
        }

        NflApi(tokenURL, baseApiUrl).getWeek(week)

        verify { mockApiConnection.setRequestProperty("authorization", "Bearer $token") }
    }

    @Test
    fun gameRequestForFinalGameUpdatesResultWithWinnerCHIAtHome() {
        val gameUuid = "10160000-dd69-64b5-f7c3-0be4babbf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val game = baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "FINAL"
                    homePointsTotal = 107
                    visitorPointsTotal = 0
                    homeTeam = GameTeam("CHI")
                    visitorTeam = GameTeam("GB")
                }
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(game)
            .byteInputStream()

        val gameDTO = GameDTO("GB@CHI", "Week 4").apply {
            id = UUID.fromString(gameUuid)
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals("CHI", result.result)
    }

    @Test
    fun gameRequestHasCopiedId() {
        val gameUuid = "10160000-dd69-64b5-f7c3-0be4babbf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(defaultGame())
            .byteInputStream()
        val gameDTO = GameDTO("GB@CHI", "Week 4").apply {
            id = UUID.fromString(gameUuid)
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals(gameUuid, result.id.toString())
    }

    @Test
    fun gameRequestHasCopiedTime() {
        val gameUuid = "10160000-dd69-64b5-f7c3-0be4babbf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(defaultGame())
            .byteInputStream()
        val gameDTO = GameDTO("GB@CHI", "Week 4").apply {
            id = UUID.fromString(gameUuid)
            gameTime = OffsetDateTime.of(
                2020, 5, 17,
                0, 3, 55, 2000,
                ZoneOffset.ofHours(-5)
            )
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals(gameDTO.gameTime, result.gameTime)
    }

    @Test
    fun gameRequestForFinalOvertimeGameUpdatesResultWithWinnerCHIAtHome() {
        val gameUuid = "10160000-dd69-64b5-f7c3-0be4babbf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val game = baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "FINAL_OVERTIME"
                    homePointsTotal = 7
                    visitorPointsTotal = 3
                    homeTeam = GameTeam("CHI")
                    visitorTeam = GameTeam("GB")
                }
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(game)
            .byteInputStream()
        val gameDTO = GameDTO("GB@CHI", "Week 4").apply {
            id = UUID.fromString(gameUuid)
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals("CHI", result.result)
    }

    @Test
    fun gameRequestForFinalGameUpdatesResultWithWinnerTBAway() {
        val gameUuid = "1016aa00-05f9-64b5-f7c3-0be4baabf0ff"
        val uri = buildGameQueryUrl(gameUuid)

        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)

        val game = baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "FINAL"
                    homePointsTotal = 3
                    visitorPointsTotal = 17
                    homeTeam = GameTeam("NE")
                    visitorTeam = GameTeam("TB")
                }
            )
        }

        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(game)
            .byteInputStream()

        val gameDTO = GameDTO("TB@NE", "Week 12").apply {
            id = UUID.fromString(gameUuid)
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals(result.name, gameDTO.name)
        assertEquals(result.week, gameDTO.week)
        assertEquals("TB", result.result)
    }

    @Test
    fun gameRequestForFinalGameUpdatesResultWithTIE() {
        val gameUuid = "1016000c-0569-6425-f7c3-0be4baabfaff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val game = baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "FINAL"
                    homePointsTotal = 3
                    visitorPointsTotal = 3
                    homeTeam = GameTeam("NE")
                    visitorTeam = GameTeam("TB")
                }
            )
        }
        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(game)
            .byteInputStream()
        val gameDTO = GameDTO("CLE@CIN", "Week 7").apply {
            id = UUID.fromString(gameUuid)
        }

        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals("TIE", result.result)
    }

    @Test
    fun gameRequestForInProgressGameDoesNotHaveResult() {
        val gameUuid = "10160000-0569-3333-f7c3-0be4baabf0ff"
        val uri = buildGameQueryUrl(gameUuid)

        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)

        val game = baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "In progress"
                    homePointsTotal = 17
                    visitorPointsTotal = 3
                    homeTeam = GameTeam("NE")
                    visitorTeam = GameTeam("TB")
                }
            )
        }

        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(game)
            .byteInputStream()

        val gameDTO = defaultGameDTO(gameUuid)
        val result: GameDTO = NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        assertEquals(result.name, gameDTO.name)
        assertEquals(result.week, gameDTO.week)
        assertEquals(null, result.result)
    }

    @Test
    fun gameRequestHasStaticHeadersSet() {
        val gameUuid = "10160000-0569-64b5-f7c3-0be4b4abf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)

        every { mockApiConnection.inputStream } returns
                ObjectMapper().writeValueAsString(defaultGame()).byteInputStream()

        NflApi(tokenURL, baseApiUrl).getGame(defaultGameDTO(gameUuid))

        val properties = ArrayList<String>(5).apply {
            add("authority")
            add("origin")
            add("accept")
            add("referer")
            add("user-agent")
            add("Content-Type")
        }
        properties.map { property ->
            verify { mockApiConnection.setRequestProperty(property, any()) }
        }
    }

    @Test
    fun gameRequestHasBearerTokenSet() {
        val gameUuid = "10160000-0569-64b5-f7c3-0be4baabf0ff"
        val uri = buildGameQueryUrl(gameUuid)
        handler.setConnection(URL(baseApiUrl, uri), mockApiConnection)
        val token = generateExpiringToken(4)
        every { mockTokenConnection.inputStream } returns buildByteStreamResponse(token)

        every { mockApiConnection.inputStream } returns ObjectMapper().writeValueAsString(defaultGame())
            .byteInputStream()

        val gameDTO = defaultGameDTO(gameUuid)
        NflApi(tokenURL, baseApiUrl).getGame(gameDTO)

        verify { mockApiConnection.setRequestProperty("authorization", "Bearer $token") }
    }

    private fun defaultGameDTO(gameUuid: String): GameDTO {
        return GameDTO("TB@NE", "Week 14").apply {
            id = UUID.fromString(gameUuid)
        }
    }

    private val defaultGameStart = OffsetDateTime.of(
        2020, 4, 25,
        12, 20, 55, 0,
        ZoneOffset.of("-0500")
    )

    private fun defaultGame(): GameQueryDTO {
        return baseGameQueryDTO.apply {
            data.viewer.gameDetailsByIds = listOf(
                Details().apply {
                    phase = "In progress"
                    homePointsTotal = 17
                    visitorPointsTotal = 3
                    homeTeam = GameTeam("NE")
                    visitorTeam = GameTeam("TB")
                }
            )
        }
    }

    private fun buildGameQueryUrl(gameUuid: String) =
        "/v3/shield/?query=query%7Bviewer%7BgameDetailsByIds(ids%3A%5B%22$gameUuid%22%2C%5D)%7Bid%2Cphase%2ChomePointsTotal%2CvisitorPointsTotal%2Cphase%2ChomeTeam%7Babbreviation%7D%2CvisitorTeam%7Babbreviation%7D%7D%7D%7D&variables=null\n"

    private fun buildRelativeApiWeekQueryUrl(
        season: Int,
        weekTypeQuery: String,
        weekQuery: Int
    ): String {
        return "/v3/shield/?query=query%7Bviewer%7Bleague%7Bgames(first%3A100%2Cweek_seasonValue%3A${season}%2Cweek_seasonType%3A${weekTypeQuery}%2Cweek_weekValue%3A${weekQuery}%2C)%7Bedges%7Bnode%7BgameDetailId%20gameTime%20awayTeam%7BnickName%20abbreviation%20%7DhomeTeam%7BnickName%20abbreviation%20%7D%7D%7D%7D%7D%7D%7D&variables=null"
    }

    private fun nflServiceWithFixedTime(url: URL, token: String? = null): NflApi {
        val service = NflApi(url, baseApiUrl).apply {
            now = absoluteTime
        }
        if (token != null) {
            service.accessToken = token
        }
        return service
    }

    private fun generateExpiringToken(hoursToExpiration: Int): String {
        val algorithmHS: Algorithm = Algorithm.HMAC256("secret")

        val issued = absoluteTime()
        val expires = with(GregorianCalendar()) {
            time = issued
            add(Calendar.HOUR, hoursToExpiration)
            time
        }

        return JWT.create()
            .withIssuedAt(issued)
            .withExpiresAt(expires)
            .withClaim("clientId", "xxx")
            .sign(algorithmHS)
    }

    private fun buildByteStreamResponse(expectedToken: String) = buildTokenResponse(expectedToken).byteInputStream()

    private val baseGameQueryDTO: GameQueryDTO
        get() {
            return GameQueryDTO().apply {
                data = GameData().apply {
                    viewer = GameViewer().apply {
                        gameDetailsByIds = ArrayList(0)
                    }
                }
            }
        }

    private val baseWeekQueryDTO: WeekQueryDTO
        get() {
            return WeekQueryDTO().apply {
                data = WeekData().apply {
                    viewer = WeekViewer().apply {
                        league = League().apply {
                            games = Games().apply {
                                edges = ArrayList(0)
                            }
                        }
                    }
                }
            }
        }

    private fun buildGame(away: String, home: String, time: OffsetDateTime): Edge {
        return Edge().apply {
            node = Node().apply {
                gameDetailId = UUID.fromString("10012016-1006-0091-2590-6d5ccb310545")
                gameTime = time.toString()
                awayTeam = Team().apply {
                    nickName = "Cardinals"
                    abbreviation = away
                }
                homeTeam = Team().apply {
                    nickName = "49ers"
                    gameDetailId = UUID.fromString("10044500-2016-98ea-5998-12e71471f00f")
                    abbreviation = home
                }
            }
        }
    }

    @Suppress("unused")
    private fun buildTokenResponse(expectedToken: String): String {
        return ObjectMapper().writeValueAsString(object {
            val access_token = expectedToken
            val expires_in = 3600
            val refresh_token = null
            val scope = null
            val token_type = "Bearer"
        })
    }
}
