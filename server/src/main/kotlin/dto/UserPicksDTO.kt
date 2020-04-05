package dto

import dto.PickDTO
import dto.UserDTO

class UserPicksDTO(var user: UserDTO) {
    var total: Int = 0
    var picks: ArrayList<PickDTO> = ArrayList(3)

    init {
//        picks.add(PickDTO("GB@CHI", "CHI"))
//        picks.add(PickDTO("BUF@NE", "BUF"))
//        picks.add(PickDTO("SEA@PHI", "SEA"))
    }
}