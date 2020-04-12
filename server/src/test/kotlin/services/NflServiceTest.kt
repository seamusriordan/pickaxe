package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

class NflServiceTest {
    @Test
    fun shouldGetApiTokenWithGetAccessToken() {
        val expectedToken = "token"
        val mockUrl = mockkClass(URL::class)

        val mockUrlConnection = mockkClass(HttpURLConnection::class)
        every { mockUrl.openConnection() } returns mockUrlConnection
        every { mockUrlConnection.inputStream } returns expectedToken.byteInputStream()

        val service = NflService()

        val token = service.getAccessToken()

        assertEquals(expectedToken, token)
    }
}