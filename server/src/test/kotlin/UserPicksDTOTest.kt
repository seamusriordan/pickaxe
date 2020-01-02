import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserPicksDTOTest {
    @Test
    fun hasUserAndPickListAndTotal() {
        var userPicks = UserPicksDTO(UserDTO("Some dude"));

        val picks: List<PickDTO> = userPicks.picks

        assertEquals(userPicks.user.name, "Some dude")
        assertEquals(3, picks.size)
        assertEquals(0, userPicks.total)
    }
}