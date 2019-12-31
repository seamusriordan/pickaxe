export function findByClassName(grid, className) {
    return grid.findAll(
        el => {
            return el.props.className === className
        });
}

export function assertAllUserPicksMatchCellText(queryResult, pickCells) {
    queryResult.users.map(user =>
        assertUserPicksMatchCells(user, queryResult.games, pickCells)
    );
}

function assertUserPicksMatchCells(user, games, pickCells) {
    games.map(
        game => assertUserPickForGameMatchesCellText(user, game, pickCells)
    )
}

function assertUserPickForGameMatchesCellText(user, game, pickCells) {
    const pickCell = firstCellThatMatchesID(pickCells, pickCellID(user, game));

    expect(pickByGame(user.picks, game.name)).toEqual(pickCell.props.children);
}

function firstCellThatMatchesID(pickCells, pickCellID) {
    const cellsWithMatchingID = pickCells.filter(
        cell => cell.props.id === pickCellID
    );
    return cellsWithMatchingID[0];
}

function pickCellID(user, game) {
    return user.name + '-' + game.name;
}

function pickByGame(picks, game) {
    return picks.filter(pick => pick["game"] === game)[0]["pick"]
}
