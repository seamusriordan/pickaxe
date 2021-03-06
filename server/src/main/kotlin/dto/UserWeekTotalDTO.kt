package dto

class UserWeekTotalDTO(var user: UserDTO) {
    var games: MutableList<GameDTO> = ArrayList(0)
    val total: Int
        get() {
            return games.size
        }
}