package dto

import dto.GameDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameDTOTest {
    @Test
    fun hasNameFromInstantiation() {
        val game = GameDTO("AWY@HOM")
        Assertions.assertEquals("AWY@HOM", game.name)
    }

    @Test
    fun hasResult() {
        val game = GameDTO("AWY@HOM")
        Assertions.assertEquals("", game.result)
    }

    @Test
    fun hasSpread() {
        val game = GameDTO("AWY@HOM")
        Assertions.assertEquals("", game.spread)
    }
}