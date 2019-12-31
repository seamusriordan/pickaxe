import React from "react";

export default function PickCell(props){
    return <div contentEditable={true}
                onKeyPress={props.sendData}
                onBlur={props.sendData}
                suppressContentEditableWarning="true"
    >{props.pick}</div>
}
