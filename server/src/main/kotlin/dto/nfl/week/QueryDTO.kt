package dto.nfl.week

import java.util.*
import kotlin.collections.ArrayList

class QueryDTO {
    var data = Data()
}

class Data {
    var viewer = Viewer()
}

class Viewer {
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
    var id = UUID.randomUUID()
    var awayTeam = Team()
    var homeTeam = Team()
}

class Team {
    var nickName: String = ""
    var abbreviation: String = ""
    var id: UUID? = null
}
