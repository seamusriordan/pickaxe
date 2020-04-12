package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

class NflServiceTest {
    @Test
    fun shouldGetApiTokenWithGetAccessTokenoftoken() {
        val expectedToken = "token"

        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()

        val service = NflService(mockUrlConnection)

        val token = service.getAccessToken()

        assertEquals(expectedToken, token)
    }

    @Test
    fun shouldGetApiTokenWithGetAccessTokenoflongtoken() {
        val expectedToken = "long token"

        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()

        val service = NflService(mockUrlConnection)

        val token = service.getAccessToken()

        assertEquals(expectedToken, token)
    }
}