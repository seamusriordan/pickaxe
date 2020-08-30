import React from "react";

const LinearCells = props => {
    const {items, name} = props;
    let cells = items.map((item, index) => {
        let cssClass = `grid__cell grid__cell--${name} grid__cell--border ${name}-linear-cell `

        return <div
            className={cssClass}
            key={`${name}-${index}`}
            id={`${name}-${index}`}>
            {item}
        </div>
    });
    return !items ? undefined : <div className="grid__column">{cells}</div>;
}

export default LinearCells