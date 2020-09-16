package services

import io.mockk.every
import io.mockk.mockkClass
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
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
    private val userAgentSlot = slot<String>()

    init {
        handler.setConnection(picksUrl, mockPicksConnection)

        every { mockPicksConnection.requestMethod = "GET" } returns Unit
        every { mockPicksConnection.doOutput = true } returns Unit
        every { mockPicksConnection.setRequestProperty("user-agent", capture(userAgentSlot)) } returns Unit
    }

    @Test
    fun urlOpenConnectionIOExceptionReturnsNoPicks() {
        val mockUrl = mockkClass(URL::class)
        every { mockUrl.openConnection() } throws IOException("Mock io exception")
        val picksApi = VegasPicksApi(mockUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(0, picks.size)
    }

    @Test
    fun `getVegasPicks uses user agent`() {
        every { mockPicksConnection.inputStream } returns "".byteInputStream()
        val picksApi = VegasPicksApi(picksUrl)

        picksApi.getVegasPicks()

        assertEquals(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36",
            userAgentSlot.captured
        )
    }

    @Test
    fun getInputStreamIOExceptionReturnsNoPicks() {
        every { mockPicksConnection.inputStream } throws IOException("Mock io exception")
        val picksApi = VegasPicksApi(picksUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(0, picks.size)
    }

    @Test
    fun getVegasPickWithNotDataReturnsNoGames() {
        every { mockPicksConnection.inputStream } returns "".byteInputStream()
        val picksApi = VegasPicksApi(picksUrl)

        val picks = picksApi.getVegasPicks()

        assertEquals(0, picks.size)
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
    fun getVegasPickWithPositiveSpreadHasAwayTeamAsWinner() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Pittsburgh",
                "Dallas",
                "&nbsp;<br>-1&nbsp;-10<br>49u-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        val pick = picks.first()
        assertEquals("PIT", pick.pick)
    }

    @Test
    fun getVegasPickParsesWithPositiveSpread1() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Pittsburgh",
                "Dallas",
                "&nbsp;<br>-1&nbsp;-10<br>49u-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        val pick = picks.first()
        assertEquals(1.0, pick.spread)
    }

    @Test
    fun getVegasPickWithPositiveSpreadReturnsValueWithSpread() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Pittsburgh",
                "Dallas",
                "&nbsp;<br>-3&frac12&nbsp;-10<br>49u-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        val pick = picks.first()
        assertEquals(3.5, pick.spread)
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
        assertEquals(-10.0, pick.spread)
    }

    @Test
    fun pickTokenWithOverParsesCorrectly() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                "&nbsp;<br>41&frac12;o-15<br>-1&frac12;&nbsp;-15"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(-1.5, picks.first().spread)
    }

    @Test
    fun pickTokenWithPKReturnsSpread0() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                "&nbsp;<br>46&frac12;u-10<br>PK&nbsp;-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0.0, picks.first().spread)
    }

    @Test
    fun pickTokenWithPKReturnsPickTIE() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                "&nbsp;<br>46&frac12;u-10<br>PK&nbsp;-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals("TIE", picks.first().pick)
    }

    @Test
    fun emptyPickCellDoesNotYieldPick() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                ""
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0, picks.size)
    }

    @Test
    fun cellWithSingleTokenDoesNotYieldPick() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Houston",
                "Kansas City",
                "o"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0, picks.size)
    }


    @Test
    fun `cell with one unknown team does not yield pick`() {
        every { mockPicksConnection.inputStream } returns buildSampleRow(
            VegasData(
                "Huston",
                "Kansas City",
                "<br>56&frac12;u-10<br>-10&nbsp;-10"
            )
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0, picks.size)
    }


    @Test
    fun `cell with one team does not yield pick`() {
        every { mockPicksConnection.inputStream } returns wrapWithTable(
            "<tr>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 gameCell\">\n" +
                    "        <span class=\"cellTextHot\">08/06  8:00 PM</span><br>\n" +
                    "        <a class=\"tabletext\">Chicago</a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            &nbsp;<br>-3&nbsp;-10<br>40u-10\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            <br>56&frac12;u-10<br>-10&nbsp;-10\"\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "</tr>\n"
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0, picks.size)
    }


    @Test
    fun `cell with non-double string does not create pick`() {
        every { mockPicksConnection.inputStream } returns wrapWithTable(
            "<tr>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 gameCell\">\n" +
                    "        <span class=\"cellTextHot\">08/06  8:00 PM</span><br>\n" +
                    "        <a class=\"tabletext\">Chicago</a>\n" +
                    "        <a class=\"tabletext\">Detroit</a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            &nbsp;<br>-3&nbsp;-10<br>40u-10\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "    <td class=\"viCellBg1 cellTextNorm cellBorderL1 center_text nowrap oddsCell\">\n" +
                    "        <a class=\"cellTextNorm\">\n" +
                    "            <br>56&frac12;u-10<br>-f&nbsp;-10\"\n" +
                    "        </a>\n" +
                    "    </td>\n" +
                    "</tr>\n"
        ).byteInputStream()

        val picks = VegasPicksApi(picksUrl).getVegasPicks()

        assertEquals(0, picks.size)
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