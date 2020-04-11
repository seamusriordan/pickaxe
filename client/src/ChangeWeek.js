import React from "react";

const ChangeWeek = props => {
    const {week} = props;

    return <div>
        <div id="changeWeek-back">Back</div>
        <div id="changeWeek-week">{week}</div>
        <div id="changeWeek-forward">Next</div>
    </div>
}

export default ChangeWeek
