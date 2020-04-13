package dto

class UserPicksDTO(var user: UserDTO) {
    var picks: ArrayList<PickDTO> = ArrayList(0)
}