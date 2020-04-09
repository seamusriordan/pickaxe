import React from "react";

export function RowOrColumnCells(items, name) {
    const className = `${name}-cell`
    return !items ? undefined :
        items.map((item, index) => {
            return <div className={className} key={index}>{item}</div>
        });
}