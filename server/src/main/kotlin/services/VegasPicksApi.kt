package services

import dto.PickWithSpreadDTO
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.IntStream.range

class VegasPicksApi(private val url: URL) {
    fun getVegasPicks(): List<PickWithSpreadDTO> {
        val connection = url.openConnection()
        val stream = connection.getInputStream()
        val response = InputStreamReader(stream).readText()

        return parseResponseToGames(response)
    }

    private fun parseResponseToGames(response: String): List<PickWithSpreadDTO> {
        val document = Jsoup.parse(response)

        val gameCells = document.select("td.gameCell")
        val oddsCells = document.select("td.gameCell + td.oddsCell + td.oddsCell")

        val vegasData = mutableListOf<PickWithSpreadDTO>()
        for (i in range(0, gameCells.size)) {
            val gameCell = gameCells[i]
            val oddsCell = oddsCells[i]

            val teams = gameCell.select(".tabletext").map { cell -> translateToTeamAbbrev(cell.text()) }
            val oddsString = oddsCell.text()

            val spread = parseSpread(oddsString)

            var pick = teams[1]
            if(spread > 0){
                pick = teams[0]
            }

            vegasData.add(
                PickWithSpreadDTO("${teams[0]}@${teams[1]}", pick, spread)
            )

        }

        return vegasData
    }

    private fun parseSpread(oddsString: String): Double {
        val tokens = oddsString.split(Regex("\\s"))

        val uPosition = tokens.indexOfFirst { it.contains('u') }

        if(uPosition == 0){
            val spreadToken = tokens[1]
            if(spreadToken.contains('½')){
                return spreadToken.substringBefore('½').toDouble() - 0.5
            }
            return tokens[1].toDouble()
        }

        return 1.0
    }

    private fun translateToTeamAbbrev(longTeamName: String): String {
        val translationMap = mapOf(
            "Arizona" to "ARI",
            "Atlanta" to "ATL",
            "Baltimore" to "BAL",
            "Buffalo" to "BUF",
            "Carolina" to "CAR",
            "Cincinnati" to "CIN",
            "Chicago" to "CHI",
            "Cleveland" to "CLE",
            "Dallas" to "DAL",
            "Denver" to "DEN",
            "Detroit" to "DET",
            "Green Bay" to "GB",
            "Houston" to "HOU",
            "Indianapolis" to "IND",
            "Jacksonville" to "JAX",
            "Kansas City" to "KC",
            "Las Vegas" to "LAV",
            "L.A. Chargers" to "LAC",
            "L.A. Rams" to "LAR",
            "Miami" to "MIA",
            "Minnesota" to "MIN",
            "New England" to "NE",
            "New Orleans" to "NO",
            "N.Y. Giants" to "NYG",
            "N.Y. Jets" to "NYJ",
            "Philadelphia" to "PHI",
            "Pittsburgh" to "PIT",
            "San Francisco" to "SF",
            "Seattle" to "SEA",
            "Tampa Bay" to "TB",
            "Tennessee" to "TEN",
            "Washington" to "WAS"
        )

        return translationMap[longTeamName] ?: longTeamName
    }
}
