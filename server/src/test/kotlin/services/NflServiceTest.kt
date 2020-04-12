package services

import com.auth0.jwt.JWT
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper


class NflServiceTest {
    private val handler = MockURLStreamHandler
    private val tokenURL = URL("https://tokenendpoint")
    private val mockUrlConnection = mockkClass(HttpURLConnection::class)

    @BeforeEach
    fun beforeEach(){
        handler.setConnection(tokenURL, mockUrlConnection)
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfToken() {
        val algorithmHS: Algorithm = Algorithm.HMAC256("secret")
        val expectedToken = JWT.create().withIssuer("me").sign(algorithmHS)
        val json = buildTokenResponse(expectedToken)
        every { mockUrlConnection.inputStream } returns json.byteInputStream()

        val token = NflService(tokenURL).accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfLongToken() {
        val algorithmHS: Algorithm = Algorithm.HMAC256("secret")
        val expectedToken = JWT.create().withIssuer("someone else").sign(algorithmHS)
        val json = buildTokenResponse(expectedToken)
        every { mockUrlConnection.inputStream } returns json.byteInputStream()

        val token = NflService(tokenURL).accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun ifAccessTokenIsSetAndValidDoNotFetchNewToken() {
        val expectedToken = "Expected Token"
        val nflService = NflService(tokenURL)
        nflService.accessToken = expectedToken
        val algorithmHS: Algorithm = Algorithm.HMAC256("secret")
        val unexpectedToken = JWT.create().withIssuer("me").sign(algorithmHS)
        val json = buildTokenResponse(unexpectedToken)
        every { mockUrlConnection.inputStream } returns json.byteInputStream()

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
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