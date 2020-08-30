import React from "react";
import {LeaderboardRow} from "./LeaderboardRow";
import "./Leaderboard.css"

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
    return <div key="grid-leaders" className="leaderboard__container">
        <div className="grid__cell grid__cell--leader grid__cell--border-bottom leaderboard__row leaderboard__row--name">Leaders</div>
        <div className="grid__cell grid__cell--leader grid__cell--border-bottom leaderboard__row leaderboard__row--numerical">Weeks Won</div>
        <div className="grid__cell grid__cell--leader grid__cell--border-bottom leaderboard__row leaderboard__row--numerical">Total Correct</div>
        {rows}
    </div>
}