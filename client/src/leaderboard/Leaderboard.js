import React from "react";
import {LeaderboardRow} from "./LeaderboardRow";

export function Leaderboard({data}) {
    return data
        .sort((a, b) => b.correctPicks - a.correctPicks)
        .sort((a, b) => b.correctWeeks - a.correctWeeks)
        .map((leader, index) =>
            <LeaderboardRow key={`leader-${index}`}
                            testId={`leader-${index}`}
                            name={leader.name}
                            weeks={leader.correctWeeks}
                            picks={leader.correctPicks}/>
        )
}