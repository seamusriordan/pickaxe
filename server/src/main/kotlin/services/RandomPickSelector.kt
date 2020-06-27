package services

class RandomPickSelector {
    fun chooseRandomFor(game: String): String {
        val teams = game.split("@")
        return teams.random()
    }
}
