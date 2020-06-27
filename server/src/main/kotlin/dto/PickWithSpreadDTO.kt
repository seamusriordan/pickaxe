package dto

class PickWithSpreadDTO(
    game: String,
    pick: String,
    val spread: Double
): PickDTO(game, pick)
