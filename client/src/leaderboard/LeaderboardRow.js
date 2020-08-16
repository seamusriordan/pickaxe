import React from "react";
import "./LeaderboardRow.css"

export function LeaderboardRow(props) {
    return <div>
        <div className="leader-name grid-cell border-cell leader-cell leader-name">{props.name}</div>
        <div className="leader-correct-weeks grid-cell border-cell leader-cell leader-numerical">{props.weeks}</div>
        <div className="leader-correct-picks grid-cell border-cell leader-cell leader-numerical">{props.picks}</div>
        <div className="leader-cell grid-cell border-cell right-padding-cell"/>
    </div>
}