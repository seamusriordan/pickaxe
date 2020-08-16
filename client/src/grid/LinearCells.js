import React from "react";

const LinearCells = props => {
    const {items, name} = props;
    let cells = items.map((item, index) => {
        let cssClass = `${name}-cell grid-cell border-cell`

        return <div
            className={cssClass}
            key={`${name}-${index}`}
            id={`${name}-${index}`}>
            {item}
        </div>
    });
    return !items ? undefined : cells;
}

export default LinearCells