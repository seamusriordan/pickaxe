package dto

import dto.GameDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameDTOTest {
    @Test
    fun hasNameAndWeekFromInstantiation() {
        val game = GameDTO("AWY@HOM", "0")
        Assertions.assertEquals("AWY@HOM", game.name)
        Assertions.assertEquals("0", game.week)
    }

    @Test
    fun hasDefaultResultNull() {
        val game = GameDTO("AWY@HOM", "0")
        Assertions.assertEquals(null, game.result)
    }

    @Test
    fun hasDefaultSpreadNull() {
        val game = GameDTO("AWY@HOM", "0")
        Assertions.assertEquals(null, game.spread)
    }
}