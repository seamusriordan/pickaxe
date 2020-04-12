import PickCell from "./PickCell";
import React from "react";

const PickCells = props => {
    const {data, sendData, currentWeek} = props;
    return (!data.users || !data.games) ? undefined :
        data.users.map((user, index1) => {
            return data.games.map((game, index2) => {
                let pickSet = getPicksForUser(data.userPicks, user.name);
                let thisPick = getPickByGame(pickSet, game.name);

                const sendDataCallback = (event, updatedPick) => {
                    sendData({
                        variables: {
                            name: user.name,
                            week: currentWeek,
                            game: game.name,
                            pick: updatedPick,
                        }
                    });
                };

                return <PickCell
                    className="pick-cell"
                    id={`${user.name}-${game.name}`}
                    key={`${index1}-${index2}`}
                    game={game.name}
                    pick={thisPick}
                    user={user.name}
                    sendData={sendDataCallback}
                />
            });
        });
}

export default PickCells

export function getPickByGame(passedPicks, gameName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pick => pick["game"] === gameName)[0];

    return firstMatchingPick ? firstMatchingPick["pick"] : null
}

export function getPicksForUser(passedPicks, userName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pickSet => pickSet.user.name === userName)[0];

    return firstMatchingPick ? firstMatchingPick.picks : null
}

