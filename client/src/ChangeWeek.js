import React from "react";
import './ChangeWeek.css'
import arrowForward from './arrow_forward_ios-24px.svg'

const ChangeWeek = props => {
    const {week, forward, back} = props;

    return <div>
        <img src={arrowForward} id="changeWeek-back" className="change-week-element" onClick={back} alt="Back"/>
        <div id="changeWeek-week" className="change-week-element change-week-week">{`${week}`}</div>
        <img src={arrowForward} id="changeWeek-forward" onClick={forward} className="change-week-element" alt="Next"/>
    </div>
}

export default ChangeWeek