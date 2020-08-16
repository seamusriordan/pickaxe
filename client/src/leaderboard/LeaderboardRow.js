import React from "react";

export function LeaderboardRow(props) {
    return <div>
        <div className="leader-name grid-cell border-cell leader-cell">{props.name}</div>
        <div className="leader-correct-weeks grid-cell border-cell leader-cell">{props.weeks}</div>
        <div className="leader-correct-picks grid-cell border-cell leader-cell">{props.picks}</div>
        <div className="leader-cell grid-cell border-cell right-padding-cell"/>
    </div>
}