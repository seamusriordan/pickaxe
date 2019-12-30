import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserDTOTest {
    @Test
    fun userDTOHasNameFromInstantiation() {
        val user = UserDTO("some name");
        assertEquals("some name", user.name)
    }
}