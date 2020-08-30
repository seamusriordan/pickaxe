import PickCell from "./PickCell";
import React from "react";
import {getPickByGame} from "./UserPicksGrid";

const UserPickColumn = props => {
    return props.games.map(game => {
        let thisPick = getPickByGame(props.pickSet, game.name);

        const sendDataCallback = (event, updatedPick) => {
            props.sendData(props.user.name, game.name, updatedPick)
        };
        return <PickCell
            className="pick-cell grid-cell"
            id={`${props.user.name}-${game.name}`}
            key={`${props.userIndex}-${game.name}`}
            game={game.name}
            pick={thisPick}
            user={props.user.name}
            sendData={sendDataCallback}
        />
    });
}

export default UserPickColumn