package services

import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

class VegasPicksApiTest {
    private val sampleRow = buildSampleRow(
        VegasData(
            "Pittsburgh",
            "Dallas",
            "&nbsp;<br>34u-10<br>-1&frac12;&nbsp;-10"
        )
    )


    private val handler = MockURLStreamHandler
    private val picksUrl = URL("https://picksendpoint")
    private val mockPicksConnection = mockkClass(HttpURLConnection::class)

    init {
        handler.setConnection(picksUrl, mockPicksConnection)

        every { mockPicksConnection.requestMethod = "GET" } returns Unit
        every { mockPicksConnection.doOutput = true } returns Unit
    }

    @Test
    fun getVegasPickWithNotDataReturnsNoGames() {
        every { mockPicksConnection.inputStream } returns "".byteInputStream()
        val picksApi = VegasPicksApi(picksUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(0, picks.size)
    }

    @Test
    fun teamWithBadNameTranslatesIdentically() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Pittsburgh",
                "Dertroid",
                "<br>56&frac12;u-10<br>-10&nbsp;-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals("PIT@Dertroid", picks.first().game)
    }


    @Test
    fun twoPicksInDataWillReturnTwoPicks() {
        every { mockPicksConnection.inputStream } returns buildSampleRows(
            listOf(
                VegasData(
                    "Pittsburgh",
                    "Dallas",
                    "<br>56&frac12;u-10<br>-10&nbsp;-10"
                ),
                VegasData(
                    "Houston",
                    "Kansas City",
                    "<br>56&frac12;u-10<br>-10&nbsp;-10"
                )
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(2, picks.size)
        assertEquals("PIT@DAL", picks[0].game)
        assertEquals("HOU@KC", picks[1].game)
    }

    @Test
    fun getVegasPickWithSampleRowPitAtDalReturnsGame() {
        every { mockPicksConnection.inputStream } returns sampleRow.byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(1, picks.size)
        val pick = picks.first()
        assertEquals("PIT@DAL", pick.game)
        assertEquals("DAL", pick.pick)
        assertEquals(-1.5, pick.spread)
    }

    @Test
    fun getVegasPickWithSampleRowHouAtKcReturnsGame() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                "<br>56&frac12;u-10<br>-10&nbsp;-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(1, picks.size)
        val pick = picks.first()
        assertEquals("HOU@KC", pick.game)
        assertEquals("KC", pick.pick)
        assertEquals(-10.5, pick.spread)
    }

    private inner class VegasData(val awayTeam: String, val homeTeam: String, val spread: String)

    private fun buildSampleRow(vegasData: VegasData): String {
        return wrapWithTable(
            "<tr>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 gameCell\">\n" +
                    "        <span class=\"cellTextHot\">08/06  8:00 PM</span><br>\n" +
                    "        <a class=\"tabletext\">${vegasData.awayTeam}</a>\n" +
                    "        <a class=\"tabletext\">${vegasData.homeTeam}</a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            &nbsp;<br>-3&nbsp;-10<br>40u-10\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            ${vegasData.spread}\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "</tr>\n"
        )
    }

    private fun wrapWithTable(inner: String): String {
        return "<table>$inner</table>"
    }

    private fun buildSampleRows(vegasData: List<VegasData>): String {
        return wrapWithTable(
            vegasData.map { buildSampleRow(it) }.reduce { allRows, datum -> "$allRows${datum}" }
        )
    }
}