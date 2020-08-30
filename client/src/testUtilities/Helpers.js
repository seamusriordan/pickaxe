export function findByClassName(grid, className) {
    return grid.findAll(
        el => {
            return el.props.className?.split(" ").includes(className)
        });
}

export function assertAllUserPicksMatchCellText(queryResult, pickCells) {
    queryResult.users.map(user =>
        assertUserPicksMatchCells(queryResult.userPicks, user, queryResult.games, pickCells)
    );
}

export function getPickByGame(passedPicks, gameName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pick => pick["game"] === gameName)[0];

    return firstMatchingPick ? firstMatchingPick["pick"] : null
}

function assertUserPicksMatchCells(userPicks, user, games, pickCells) {
    games.map(
        game => assertUserPickForGameMatchesCellText(userPicks, user, game, pickCells)
    )
}

function assertUserPickForGameMatchesCellText(userPicks, user, game, pickCells) {
    const pickCell = firstCellThatMatchesID(pickCells, pickCellID(user, game));

    let inputElement = pickCell.findByType('div');

    expect(pickByGame(userPicks, user.name, game.name)).toEqual(inputElement.children[0]);
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

function pickByGame(userPicks, user, game) {
    return picksForUser(userPicks, user)
        .filter(pick => pick["game"] === game)[0]["pick"]
}

function picksForUser(userPicks, userName) {
    return userPicks.filter(pickSet => pickSet.user.name === userName)[0].picks;
}
