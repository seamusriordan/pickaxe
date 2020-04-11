import React from "react";

const ChangeWeek = props => {
    const {week, forward, back} = props;

    return <div>
        <div id="changeWeek-back" onClick={back}>Back</div>
        <div id="changeWeek-week">{`Week ${week}`}</div>
        <div id="changeWeek-forward" onClick={forward}>Next</div>
    </div>
}

export default ChangeWeek
