package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

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
        val expectedToken = "token"
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()

        val token = NflService(tokenURL).accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfLongToken() {
        val expectedToken = "long token"
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()

        val token = NflService(tokenURL).accessToken

        assertEquals(expectedToken, token)
    }


    @Test
    fun ifAccessTokenIsSetAndValidDoNotFetchNewToken() {
        val expectedToken = "Expected Token"
        val nflService = NflService(tokenURL)
        nflService.accessToken = expectedToken
        every { mockUrlConnection.inputStream } returns "Unexpected Token".byteInputStream()

        val token = nflService.accessToken

        assertEquals(expectedToken, token)
    }
}