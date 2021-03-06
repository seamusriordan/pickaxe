package dto

import java.time.OffsetDateTime
import java.util.*

class GameDTO(var name: String, var week: String) {
    var result: String? = null
    var spread: Double? = null
    var id: UUID? = null
    var gameTime: OffsetDateTime? = null
}