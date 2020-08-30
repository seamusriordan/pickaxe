import React, {useEffect, useState} from "react";


function stripAfterNewline(text) {
    return /^.*/.exec(text)[0]
}

export default function PickCell(props) {
    const [innerText, setInnerText] = useState(props.pick);

    useEffect(() => {
        setInnerText(props.pick)
    }, [props.pick]);

    const callbackWrapper = (event) => {
        if (event.target.textContent === innerText) {
            return;
        }

        const updatedPick = stripAfterNewline(event.target.textContent);
        event.target.textContent = updatedPick;
        props.sendData(event, updatedPick);
        setInnerText(updatedPick)
    };

    let cssClass = 'grid__cell grid__cell--border'.concat( props.correct ? ' grid__cell--correct' : '')

    return <div contentEditable={true}
                spellCheck={false}
                onBlur={callbackWrapper}
                suppressContentEditableWarning="true"
                id={props.id}
                className={cssClass}
    >{innerText}</div>
}
