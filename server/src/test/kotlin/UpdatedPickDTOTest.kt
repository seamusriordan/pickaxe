import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdatedPickDTOTest {
    @Test
    fun canInstantiateWithFields(){
        val updatedPick = UpdatedPickDTO(0, "Game", "Pick")

        assertEquals(0, updatedPick.week)
        assertEquals("Game", updatedPick.game)
        assertEquals("Pick", updatedPick.pick)
    }
}