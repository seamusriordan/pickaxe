package dto.nfl.api.week

import java.util.*
import kotlin.collections.ArrayList

class WeekQueryDTO {
    var data = WeekData()
}

class WeekData {
    var viewer = WeekViewer()
}

class WeekViewer {
    var league = League()
}

class League {
    var games = Games()
}

class Games {
    var edges: List<Edge> = ArrayList(0)
}

class Edge {
    var node = Node()
}

class Node {
    var gameDetailId: UUID? = UUID.randomUUID()
    var gameTime: String? = null
    var awayTeam = Team()
    var homeTeam = Team()
}

class Team {
    var nickName: String = ""
    var abbreviation: String = ""
}
