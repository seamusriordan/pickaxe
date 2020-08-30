import React from "react";
import {LeaderboardRow} from "./LeaderboardRow";

export function Leaderboard({data}) {
    const rows = data
        .sort((a, b) => b.correctPicks - a.correctPicks)
        .sort((a, b) => b.correctWeeks - a.correctWeeks)
        .map((leader, index) =>
            <LeaderboardRow key={`leader-${index}`}
                            testId={`leader-${index}`}
                            name={leader.name}
                            weeks={leader.correctWeeks}
                            picks={leader.correctPicks}/>
        );
    return <div key="grid-leaders" id="grid-leaders">
        <div className="grid-cell leader-cell border-bottom leader-name">Leaders</div>
        <div className="grid-cell border-bottom leader-cell leader-numerical">Weeks Won</div>
        <div className="grid-cell border-bottom leader-cell leader-numerical">Total Correct</div>
        {rows}
    </div>
}