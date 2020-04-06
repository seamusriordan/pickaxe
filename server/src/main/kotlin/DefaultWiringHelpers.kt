import dto.GameDTO
import dto.UserDTO
import dto.UserPicksDTO

fun defaultUserList(): ArrayList<UserDTO> {
    val usersList = ArrayList<UserDTO>()
    usersList.add(UserDTO("Seamus"))
    usersList.add(UserDTO("Sereres"))
    usersList.add(UserDTO("RNG"))
    usersList.add(UserDTO("Vegas"))
    return usersList
}

fun defaultGamesList(): ArrayList<GameDTO> {
    val gamesList = ArrayList<GameDTO>()
    gamesList.add(GameDTO("GB@CHI"))
    gamesList.add(GameDTO("BUF@NE"))
    gamesList.add(GameDTO("SEA@PHI"))
    return gamesList
}

fun defaultPicksForUsers(usersList: ArrayList<UserDTO>): ArrayList<UserPicksDTO> {
    val userPicksList = ArrayList<UserPicksDTO>()
    usersList.map { userDTO ->
        userPicksList.add(UserPicksDTO(userDTO))
    }
    return userPicksList
}

fun defaultWeek0PickStore(userPicksList: ArrayList<UserPicksDTO>): ArrayList<ArrayList<UserPicksDTO>> {
    val userPickStore: ArrayList<ArrayList<UserPicksDTO>> = ArrayList(0)
    userPickStore.add(userPicksList)
    return userPickStore
}