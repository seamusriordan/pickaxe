package dto.nfl.api.game

import java.util.*


class GameQueryDTO {
    var data = GameData()
}

class GameData {
    var viewer = GameViewer()
}

class GameViewer {
    var gameDetailsByIds: List<Details> = listOf()
}

class Details {
    var id: UUID = UUID.randomUUID()
    var phase: String = ""
    var homePointsTotal: Int = 0
    var visitorPointsTotal: Int = 0
    var gameTime: String? = null
    var visitorTeam = GameTeam()
    var homeTeam = GameTeam()
}

class GameTeam(var abbreviation: String = "")