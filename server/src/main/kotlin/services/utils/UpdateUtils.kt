package services.utils

import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import java.time.OffsetDateTime

class UpdateUtils {
    companion object {
        fun gameStartedMoreThanXHoursAgo(gameTime: OffsetDateTime?, hoursAgo: Long): Boolean {
            return gameTime != null &&
                    OffsetDateTime.now().isAfter(gameTime.plusHours(hoursAgo))
        }

        fun hasGameStartInXMinutes(time: OffsetDateTime?, minutes: Long): Boolean {
            return time != null && time.isBefore(OffsetDateTime.now().plusMinutes(minutes))
        }

        fun buildMutatorEnvironment(
            userName: String,
            weekString: String,
            game: String,
            pick: String
        ): DataFetchingEnvironment {
            val arguments = HashMap<String, Any>()
            val userPick = HashMap<String, String>()
            arguments["name"] = userName
            arguments["userPick"] = userPick

            userPick["week"] = weekString
            userPick["game"] = game
            userPick["pick"] = pick

            return DataFetchingEnvironmentImpl
                .newDataFetchingEnvironment()
                .arguments(arguments)
                .build()
        }
    }
}