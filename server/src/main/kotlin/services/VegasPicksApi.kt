package services

import dto.PickWithSpreadDTO
import java.net.URL

class VegasPicksApi(private val url: URL) {
    fun getVegasPicks(): List<PickWithSpreadDTO> {
        return listOf()
    }
}
