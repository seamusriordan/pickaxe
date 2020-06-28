package services

import dto.PickWithSpreadDTO
import java.io.InputStreamReader
import java.net.URL

class VegasPicksApi(private val url: URL) {
    fun getVegasPicks(): List<PickWithSpreadDTO> {
        val connection = url.openConnection()
        val stream = connection.getInputStream()
        val response = InputStreamReader(stream).readText()

        if(response != ""){
            return listOf(
                PickWithSpreadDTO("PIT@DAL", "DAL", -1.5)
            )
        }

        return listOf()
    }
}
