package services

import com.auth0.jwt.JWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import dto.WeekDTO
import dto.nfl.week.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class NflApiTest {
    private val handler = MockURLStreamHandler
    private val tokenURL = URL("https://tokenendpoint")
    private val baseApiUrl = URL("http://apiuri")
    private val mockUrlConnection = mockkClass(HttpURLConnection::class)

    private val absoluteTime = { GregorianCalendar(2020, 3, 12, 13, 5).time }

    private val requestOutputStream = ByteArrayOutputStream()

    @BeforeEach
    fun beforeEach() {
        handler.setConnection(tokenURL, mockUrlConnection)
        every { mockUrlConnection.requestMethod = "POST" } returns Unit
        every { mockUrlConnection.outputStream } returns requestOutputStream
        every { mockUrlConnection.doOutput = true } returns Unit
        every { mockUrlConnection.setRequestProperty(any(), any()) } returns Unit
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfToken() {
        val expectedToken = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldMakePOSTToGetToken() {
        val expectedToken = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        nflService.accessToken

        verify(exactly = 1) { mockUrlConnection.requestMethod = "POST" }
    }

    @Test
    fun outputStreamPOSTsWithGrantTypeBody() {
        val expectedBody = "grant_type=client_credentials"

        val expectedToken = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        nflService.accessToken

        assertArrayEquals(expectedBody.toByteArray(), requestOutputStream.toByteArray())
    }


    @Test
    fun connectionHasCorrectPropertiesSet() {
        val expectedToken = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)
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
            verify { mockUrlConnection.setRequestProperty(property, any()) }
        }
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfLongToken() {
        val expectedToken = generateExpiringToken(2)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)
        val nflService = nflServiceWithFixedTime(tokenURL)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun ifAccessTokenIsSetAndValidDoNotFetchNewToken() {
        val expectedToken = generateExpiringToken(1)
        val nflService = nflServiceWithFixedTime(tokenURL, expectedToken)
        val unexpectedToken = generateExpiringToken(2)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(unexpectedToken)

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun gettingTokenTwiceWillOnlyFetchOnce() {
        val nflService = nflServiceWithFixedTime(tokenURL)
        val expectedToken = generateExpiringToken(1)
        val unexpectedToken = generateExpiringToken(2)

        every {
            mockUrlConnection.inputStream
        } returnsMany listOf(
            buildByteStreamResponse(expectedToken),
            buildByteStreamResponse(unexpectedToken)
        )


        nflService.accessToken
        val secondToken = nflService.accessToken

        assertEquals(expectedToken, secondToken)
        verify(exactly = 1) { mockUrlConnection.inputStream }
    }

    @Test
    fun ifAccessTokenIsSetAndInvalidFetchNewToken() {
        val expiredToken = generateExpiringToken(-1)
        val nflService = nflServiceWithFixedTime(tokenURL, expiredToken)
        val expectedToken = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(expectedToken)

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
        val uri = buildRelativeApiUrl(2020, weekTypeQuery, weekQuery)
        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(baseApiUrl, uri), mockConnection)
        val expectedGames = baseQueryDTO
        expectedGames.data.viewer.league.games.edges = listOf(
            buildGame("ARI", "SF")
        )
        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream();

        val nflService = NflApi(tokenURL, baseApiUrl)

        val week = WeekDTO("Week 5").apply {
            weekType = weekTypeQuery
            week = weekQuery
        }

        val result = nflService.getWeek(week);

        verify(exactly = 1) { mockConnection.inputStream }

        assertEquals(1, result.size)
        assertEquals("ARI@SF", result.first().name)
        assertEquals("Week 5", result.first().week)
    }

    @Test
    fun getWeekGetsGamesFromNFLWithOneGamePreseasonWeek1() {
        val weekTypeQuery = "PRE"
        val weekQuery = 1
        val uri = buildRelativeApiUrl(2020, weekTypeQuery, weekQuery)
        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(baseApiUrl, uri), mockConnection)
        val expectedGames = baseQueryDTO
        expectedGames.data.viewer.league.games.edges = listOf(
            buildGame("ARI", "SF")
        )
        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream();

        val nflService = NflApi(tokenURL, baseApiUrl)

        val week = WeekDTO("Preseason Week 1").apply {
            weekType = weekTypeQuery
            week = weekQuery
        }

        val result = nflService.getWeek(week);

        verify(exactly = 1) { mockConnection.inputStream }

        assertEquals(1, result.size)
        assertEquals("Preseason Week 1", result.first().week)
    }

    @Test
    fun getWeekWithDifferentBaseUrl() {
        val weekTypeQuery = "REG"
        val weekQuery = 5
        val differentBaseUrl = URL("https://api.nfl.com")
        val uri = buildRelativeApiUrl(2020, weekTypeQuery, weekQuery)

        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(differentBaseUrl, uri), mockConnection)
        val expectedGames = baseQueryDTO

        expectedGames.data.viewer.league.games.edges = listOf(
            buildGame("ARI", "SF")
        )

        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream();

        val nflService = NflApi(tokenURL, differentBaseUrl)

        val week = WeekDTO("Week 5").apply {
            weekType = weekTypeQuery
            week = weekQuery
        }

        val result = nflService.getWeek(week);

        verify(exactly = 1) { mockConnection.inputStream }

        assertEquals(1, result.size)
        assertEquals("ARI@SF", result.first().name)
        assertEquals("Week 5", result.first().week)
    }

    @Test
    fun getWeekGetsGamesFromNFLWithTwoGamesRegularWeek8() {
        val weekTypeQuery = "REG"
        val weekQuery = 8
        val uri = buildRelativeApiUrl(2020, weekTypeQuery, weekQuery)

        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(baseApiUrl, uri), mockConnection)
        val expectedGames = baseQueryDTO
        expectedGames.data.viewer.league.games.edges = listOf(
            buildGame("CHI", "IND"),
            buildGame("TEN", "MIA")
        )

        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream();

        val nflService = NflApi(tokenURL, baseApiUrl)

        val week = WeekDTO("Week 8").apply {
            weekType = weekTypeQuery
            week = weekQuery
        }

        val result = nflService.getWeek(week);

        verify(exactly = 1) { mockConnection.inputStream }

        assertEquals(2, result.size)
        assertEquals("CHI@IND", result[0].name)
        assertEquals("Week 8", result[0].week)
        assertEquals("TEN@MIA", result[1].name)
        assertEquals("Week 8", result[1].week)
    }

    @Test
    fun weekRequestHasStaticHeadersSet() {
        val uri = buildRelativeApiUrl(2020, "REG", 3)
        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(baseApiUrl, uri), mockConnection)

        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(baseQueryDTO).byteInputStream();
        every { mockConnection.setRequestProperty(any(), any()) } returns Unit


        val nflService = NflApi(tokenURL, baseApiUrl)
        val week = WeekDTO("Week 3").apply {
            weekType = "REG"
            week = 3
        }
        val result = nflService.getWeek(week);

        val properties = ArrayList<String>(5).apply {
            add("authority")
            add("origin")
            add("accept")
            add("referer")
            add("user-agent")
            add("Content-Type")
        }

        properties.map { property ->
            verify { mockConnection.setRequestProperty(property, any()) }
        }
    }

    @Test
    fun weekRequestHasBearerTokenSet() {
        val uri = buildRelativeApiUrl(2020, "REG", 3)
        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(baseApiUrl, uri), mockConnection)

        val token = generateExpiringToken(1)
        every { mockUrlConnection.inputStream } returns buildByteStreamResponse(token)

        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(baseQueryDTO).byteInputStream();
        every { mockConnection.setRequestProperty(any(), any()) } returns Unit


        val nflService = NflApi(tokenURL, baseApiUrl)
        val week = WeekDTO("Week 3").apply {
            weekType = "REG"
            week = 3
        }
        nflService.getWeek(week);

        verify { mockConnection.setRequestProperty("authorization", "Bearer $token") }
    }

    private fun buildRelativeApiUrl(
        season: Int,
        weekTypeQuery: String,
        weekQuery: Int
    ): String {
        val uri =
            "/v3/shield/?query=query%7Bviewer%7Bleague%7Bgames(first%3A100%2Cweek_seasonValue%3A${season}%2Cweek_seasonType%3A${weekTypeQuery}%2Cweek_weekValue%3A${weekQuery}%2C)%7Bedges%7Bnode%7Bid%20awayTeam%7BnickName%20abbreviation%20%7DhomeTeam%7BnickName%20id%20abbreviation%20%7D%7D%7D%7D%7D%7D%7D&variables=null"
        return uri
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

    private val baseQueryDTO: QueryDTO
        get() {
            return QueryDTO().apply {
                data = Data().apply {
                    viewer = Viewer().apply {
                        league = League().apply {
                            games = Games().apply {
                                edges = ArrayList(0)
                            }
                        }
                    }
                }
            }
        }

    private fun buildGame(away: String, home: String): Node {
        return Node().apply {
            id = UUID.fromString("10012016-1006-0091-2590-6d5ccb310545")
            awayTeam = Team().apply {
                nickName = "Cardinals"
                abbreviation = away
            }
            homeTeam = Team().apply {
                nickName = "49ers"
                id = UUID.fromString("10044500-2016-98ea-5998-12e71471f00f")
                abbreviation = home
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
