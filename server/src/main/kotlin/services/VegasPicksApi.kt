package services

import dto.PickWithSpreadDTO
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.IntStream.range

class VegasPicksApi(private val url: URL) {
    fun getVegasPicks(): List<PickWithSpreadDTO> {
        val response: String
        try {
            val connection = url.openConnection().apply {
                setRequestProperty(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
                )
            }

            val stream = connection.getInputStream()
            response = InputStreamReader(stream).readText()
        } catch (e: IOException){
            return listOf()
        }

        return parseResponseToGames(response)
    }

    private fun parseResponseToGames(response: String): List<PickWithSpreadDTO> {
        val document = Jsoup.parse(response)
        val gameCells = document.select("td.gameCell")
        val oddsCells = document.select("td.gameCell + td.oddsCell + td.oddsCell")

        return buildVegasData(gameCells, oddsCells)
    }

    private fun buildVegasData(
        gameCells: Elements,
        oddsCells: Elements
    ): MutableList<PickWithSpreadDTO> {
        val vegasData = mutableListOf<PickWithSpreadDTO>()
        for (i in range(0, gameCells.size)) {
            addValidPickToData(vegasData, gameCells[i], oddsCells[i])
        }
        return vegasData
    }

    private fun addValidPickToData(
        vegasData: MutableList<PickWithSpreadDTO>,
        gameCell: Element,
        oddsCell: Element
    ) {
        val teams = gameCell.select(".tabletext").map { cell -> translateToTeamAbbrev(cell.text()) }
        val oddsString = oddsCell.text()

        if (oddsString.contains(Regex("[ou]"))) {
            addPickToData(vegasData, teams, oddsString)
        }
    }

    private fun addPickToData(
        vegasData: MutableList<PickWithSpreadDTO>,
        teams: List<String>,
        oddsString: String
    ) {
        val game = "${teams[0]}@${teams[1]}"
        val spread = parseSpread(oddsString)
        val chosenPick = choosePick(spread, teams)
        vegasData.add(PickWithSpreadDTO(game, chosenPick, spread))
    }

    private fun choosePick(spread: Double, teams: List<String>): String {
        var pick = "TIE"
        if (spread > 0) {
            pick = teams[0]
        }
        if (spread < 0) {
            pick = teams[1]
        }
        return pick
    }

    private fun parseSpread(oddsString: String): Double {
        if (oddsString.contains("PK")) return 0.0

        val tokens = oddsString.split(Regex("\\s"))
        val uPosition = tokens.indexOfFirst { it.contains(Regex("[ou]")) }
        if (uPosition == 0) {
            val spreadToken = tokens[1]
            return parseSpreadToken(spreadToken)
        }
        if (uPosition == 2) {
            val spreadToken = tokens[0]
            return -parseSpreadToken(spreadToken)
        }
        return 0.0
    }

    private fun parseSpreadToken(spreadToken: String): Double {
        if (spreadToken.contains('½')) {
            return spreadToken.substringBefore('½').toDouble() - 0.5
        }
        return spreadToken.toDouble()
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
