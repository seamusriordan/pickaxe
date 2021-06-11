import PickCell from "./PickCell";
import React from "react";

const UserPickColumn = props => {
    const cells = props.games.map(game => {
        let thisPick = getPickByGame(props.pickSet, game.name);

        const sendDataCallback = (updatedPick) => {
            props.sendData(props.user.name, game.name, updatedPick)
        };
        return <PickCell
            className="pick-cell grid__cell"
            id={`${props.user.name}-${game.name}`}
            key={`${props.user.name}-${game.name}`}
            game={game.name}
            pick={thisPick}
            user={props.user.name}
            correct={!!thisPick && thisPick?.toLowerCase() === game.result?.toLowerCase()}
            sendData={sendDataCallback}
        />
    });

    return <div className="grid__column">{cells}</div>
}

export default UserPickColumn

function getPickByGame(passedPicks, gameName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pick => pick["game"] === gameName)[0];

    return firstMatchingPick ? firstMatchingPick["pick"] : null
}
