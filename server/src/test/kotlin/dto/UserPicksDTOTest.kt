package dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserPicksDTOTest {
    @Test
    fun hasUserAndPickListAndTotal() {
        val userPicks = UserPicksDTO(UserDTO("Some dude"))

        assertEquals(userPicks.user.name, "Some dude")
    }
}