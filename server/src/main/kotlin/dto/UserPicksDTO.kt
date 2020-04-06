package dto

import dto.PickDTO
import dto.UserDTO

class UserPicksDTO(var user: UserDTO) {
    var total: Int = 0
    var picks: ArrayList<PickDTO> = ArrayList(0)
}