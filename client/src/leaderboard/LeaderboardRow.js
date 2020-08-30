import React from "react";

export function LeaderboardRow(props) {
    const baseCssClasses = "grid__cell grid__cell--border grid__cell--leader"
    return <div>
        <div className={`${baseCssClasses} leaderboard__row leaderboard__row--name leader-element-name`}>{props.name}</div>
        <div className={`${baseCssClasses} leaderboard__row leaderboard__row--numerical leader-correct-weeks`}>{props.weeks}</div>
        <div className={`${baseCssClasses} leaderboard__row leaderboard__row--numerical leader-correct-picks`}>{props.picks}</div>
        <div className={`${baseCssClasses} grid__cell--right-padding`}/>
    </div>
}