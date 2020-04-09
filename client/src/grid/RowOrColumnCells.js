import React from "react";

const RowOrColumnCells = props => {
    const {items, name} = props;
    return !items ? undefined :
        items.map((item, index) => {
            return <div className={`${name}-cell`} key={`${name}-${index}`}>{item}</div>
        });
}

export default RowOrColumnCells