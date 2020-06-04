import React from "react";

export function LeaderboardRow(props) {
    return <div>
        <span className="leader-name">{props.name}</span>
        <span className="leader-correct-weeks">{props.weeks}</span>
        <span className="leader-correct-picks">{props.picks}</span>
    </div>
}