import React from "react";
import './ChangeWeek.css'
import arrowForward from './arrow_forward_ios-24px.svg'

const ChangeWeek = props => {
    const {week, forward, back} = props;

    return <div className="change-week" >
        <img src={arrowForward} id="change-week--back" className="change-week__element change-week__element--back" onClick={back} alt="Back"/>
        <div id="change-week--week" className="change-week__element change-week__element--week">{`${week}`}</div>
        <img src={arrowForward} id="change-week--forward" onClick={forward} className="change-week__element" alt="Next"/>
    </div>
}

export default ChangeWeek