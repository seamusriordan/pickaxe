import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserDTOTest {
    @Test
    fun hasNameFromInstantiation() {
        val user = UserDTO("some name")
        assertEquals("some name", user.name)
    }

}