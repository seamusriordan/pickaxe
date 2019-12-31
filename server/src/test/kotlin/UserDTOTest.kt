import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserDTOTest {
    @Test
    fun hasNameFromInstantiation() {
        val user = UserDTO("some name")
        assertEquals("some name", user.name)
    }

    @Test
    fun hasPicksField() {
        val user = UserDTO("some name")
        assertEquals(0, user.picks.size)
    }

    @Test
    fun hasTotalField() {
        val user = UserDTO("Winless")
        assertEquals(0, user.total)
    }
}