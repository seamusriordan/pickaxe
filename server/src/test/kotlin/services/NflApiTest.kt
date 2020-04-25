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
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.*
import java.util.*


class NflApiTest {
    private val handler = MockURLStreamHandler
    private val tokenURL = URL("https://tokenendpoint")
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

        val nflService = NflApi(URL("http://url"))

        val weeks = nflService.getWeeks()

        assertEquals(expectedWeeks, weeks)
    }

    @Test
    fun loadWeeksGetsGamesFromNFL() {
        val season = 2016
        val weekType = "REG"
        val week = "5"
        val uri =
            "https://api.nfl.com/v3/shield/?query=query%7Bviewer%7Bleague%7Bgames(first%3A100%2Cweek_seasonValue%3A${season}%2Cweek_seasonType%3A${weekType}%2Cweek_weekValue%3A${week}%2C)%7Bedges%7Bnode%7Bid%20awayTeam%7BnickName%20abbreviation%20%7DhomeTeam%7BnickName%20id%20abbreviation%20%7D%7D%7D%7D%7D%7D%7D&variables=null"

        val mockConnection = mockkClass(HttpURLConnection::class)
        handler.setConnection(URL(uri), mockConnection)
        val expectedGames = object {
            val data = object {
                val viewer = object {
                    val league = object {
                        val games = object {
                            val edges = listOf(
                                object {
                                    val node = object {
                                        val id = "10012016-1006-0091-2590-6d5ccb310545"
                                        val awayTeam = {
                                            val nickName = "Cardinals"
                                            val abbreviation = "ARI"
                                        }
                                        val homeTeam = object {
                                            val nickName = "49ers"
                                            val id = "10044500-2016-98ea-5998-12e71471f00f"
                                            val abbreviation = "SF"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        every { mockConnection.inputStream } returns ObjectMapper().writeValueAsString(expectedGames).byteInputStream();

        val nflService = NflApi(URL("https://api.nfl.com"))
        nflService.loadWeeks("REG", "5");

        verify(exactly = 1) { mockConnection.inputStream }
    }

    private fun nflServiceWithFixedTime(url: URL, token: String? = null): NflApi {
        val service = NflApi(url).apply {
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