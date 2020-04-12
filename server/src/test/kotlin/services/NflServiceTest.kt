package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection

class NflServiceTest {
    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfToken() {
        val expectedToken = "token"
        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()
        val service = NflService()
        service.tokenEndpoint = mockUrlConnection

        val token = service.accessToken

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenOfLongToken() {
        val expectedToken = "long token"

        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()
        val service = NflService()
        service.tokenEndpoint = mockUrlConnection

        val token = service.accessToken

        assertEquals(expectedToken, token)
    }


    @Test
    fun ifAccessTokenIsSetAndValidDoNotFetchNewToken() {
        val unexpectedToken = "Unexpected Token"
        val expectedToken = "Expected Token"

        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrlConnection.inputStream } returns unexpectedToken.byteInputStream()
        val service = NflService()
        service.tokenEndpoint = mockUrlConnection
        service.accessToken = expectedToken

        val token = service.accessToken

        assertEquals(expectedToken, token)
    }
}