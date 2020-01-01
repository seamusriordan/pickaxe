import org.junit.jupiter.api.Test

class UpdatePickMutationDTOTest {
    @Test
    fun itHasTheRightTypes() {
        UpdatePickMutationDTO("Name", UpdatedPickDTO(0, "Gamme", "piiick"))
    }
}