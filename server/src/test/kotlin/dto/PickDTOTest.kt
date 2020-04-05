package dto

import dto.PickDTO
import org.junit.jupiter.api.Test

class PickDTOTest {
    @Test
    fun hasGameAndPickFields() {
        PickDTO("CHI@GB", "CHI")
    }
}