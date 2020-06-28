package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

class VegasPicksApiTest {
    private val sampleRowPitAtDal = "<tr>\n" +
            "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 gameCell\">\n" +
            "        <span class=\"cellTextHot\">08/06  8:00 PM</span><br>\n" +
            "        <a class=\"tabletext\">Pittsburgh</a>\n" +
            "        <a class=\"tabletext\">Dallas</a>\n" +
            "    </td>\n" +
            "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
            "        <a class=\"cellTextNorm\">\n" +
            "            &nbsp;<br>-3&nbsp;-10<br>40u-10\n" +
            "        </a>\n" +
            "    </td>\n" +
            "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
            "        <a class=\"cellTextNorm\">\n" +
            "            &nbsp;<br>34u-10<br>-1&frac12;&nbsp;-10\n" +
            "        </a>\n" +
            "    </td>\n" +
            "</tr>\n"


    @Test
    fun getVegasPickWithNotDataReturnsNoGames() {
        val handler = MockURLStreamHandler
        val picksUrl = URL("https://picksendpoint")
        val mockPicksConnection = mockkClass(HttpURLConnection::class)

        handler.setConnection(picksUrl, mockPicksConnection)

        every { mockPicksConnection.requestMethod = "GET" } returns Unit
        every { mockPicksConnection.doOutput = true } returns Unit
        every { mockPicksConnection.inputStream } returns "".byteInputStream()

        val picksApi = VegasPicksApi(picksUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(0, picks.size)
    }

    @Test
    fun getVegasPickWithSampleRowReturnsGame() {
        val handler = MockURLStreamHandler
        val picksUrl = URL("https://picksendpoint")
        val mockPicksConnection = mockkClass(HttpURLConnection::class)

        handler.setConnection(picksUrl, mockPicksConnection)

        every { mockPicksConnection.requestMethod = "GET" } returns Unit
        every { mockPicksConnection.doOutput = true } returns Unit
        every { mockPicksConnection.inputStream } returns sampleRowPitAtDal.byteInputStream()

        val picksApi = VegasPicksApi(picksUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(1, picks.size)
        val pick = picks.first()
        assertEquals("PIT@DAL", pick.game)
        assertEquals("DAL", pick.pick)
        assertEquals(-1.5, pick.spread)
    }
}