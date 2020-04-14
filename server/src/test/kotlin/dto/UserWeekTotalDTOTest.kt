package dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserWeekTotalDTOTest {
    @Test
    fun instantiates(){
        UserWeekTotalDTO(UserDTO("Guy Guy-man"))
    }

    @Test
    fun hasSizeOfZeroToStart(){
        val total = UserWeekTotalDTO(UserDTO("Guy Guy-man"))

        assertEquals(0, total.total )
    }

    @Test
    fun hasSizeOfOneIfOneGameAddedToToal(){
        val userWeeklyTotal = UserWeekTotalDTO(UserDTO("Guy Guy-man"))
        userWeeklyTotal.games.add(GameDTO("GB@CHI", "Week 7"))

        assertEquals(1, userWeeklyTotal.total )
    }
}