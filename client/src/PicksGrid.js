import React from "react";
import {useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";
import PickCell from "./PickCell";

const USER_QUERY = gql`query Query { users { name picks { game pick } total } games { name spread result } }`;

function userCells(data) {
    return !data.users ? undefined :
        data.users.map((user, index) => {
            return <div className="name-cell" key={index}>{user.name}</div>
        });
}

function totalCells(data) {
    return !data.users ? undefined :
        data.users.map((user, index) => {
            return <div className="total-cell" key={index}>{user.total}</div>
        });
}

function gameCells(data) {
    return !data.games ? undefined :
        data.games.map((game, index) => {
            return <div className="game-cell" key={index}>{game.name}</div>
        });
}

function spreadCells(data) {
    return !data.games ? undefined :
        data.games.map((game, index) => {
            return <div className="spread-cell" key={index}>{game.spread}</div>
        });
}

function resultCells(data) {
    return !data.games ? undefined :
        data.games.map((game, index) => {
            return <div className="result-cell" key={index}>{game.result}</div>
        });
}

function pickCells(data) {
    return (!data.users || !data.games) ? undefined :
        data.users.map((user, index1) => {
            return data.games.map((game, index2) => {
                return <PickCell
                    className="pick-cell"
                    id={user.name + '-' + game.name}
                    key={index1 + '-' + index2}
                    game={game.name}
                    pick={getPickByGame(user.picks, game.name)}
                    user={user.name}
                    sendData={()=>{console.log("Going to send data!")}}
                />
            });
        });
}

export function getPickByGame(passedPicks, gameName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pick => pick["game"] === gameName)[0];

    return firstMatchingPick ? firstMatchingPick["pick"] : null
}

const PicksGrid = () => {
    const {loading, error, data} = useQuery(USER_QUERY);

    return <div>
        {loading ? "Loading" : error ? "Error" : !data ? "derp" :
            [
                userCells(data),
                gameCells(data),
                spreadCells(data),
                pickCells(data),
                resultCells(data),
                totalCells(data)
            ]
        }
    </div>

};

export default PicksGrid
