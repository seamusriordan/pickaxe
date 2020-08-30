import React from "react";

const LinearCells = props => {
    const {items, name} = props;
    let cells = items.map((item, index) => {
        let cssClass = `${name}-cell ${name}-linear-cell grid-cell border-cell`

        return <div
            className={cssClass}
            key={`${name}-${index}`}
            id={`${name}-${index}`}>
            {item}
        </div>
    });
    return !items ? undefined : <div className="grid-column">{cells}</div>;
}

export default LinearCells