import React from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";
import PickCell from "./PickCell";

const PICKS_QUERY = gql`query Query { users { name picks { game pick } total } games { name spread result } }`;
const UPDATE_PICKS_MUTATION =
gql`mutation Mutation($name: String, $pick: UpdatedPick)
{ updatePick(name: $name, pick: $pick)}`;

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


function pickCells(data, sendData) {
    return (!data.users || !data.games) ? undefined :
        data.users.map((user, index1) => {
            return data.games.map((game, index2) => {
                let thisPick = getPickByGame(user.picks, game.name);

                const sendDataCallback = (event, updatedPick) => {
                    sendData({ variables:
                    {
                        name: user.name,
                        pick: {
                            game: game.name,
                            pick: updatedPick
                        }
                    }});
                };

                return <PickCell
                    className="pick-cell"
                    id={user.name + '-' + game.name}
                    key={index1 + '-' + index2}
                    game={game.name}
                    pick={thisPick}
                    user={user.name}
                    sendData={sendDataCallback}
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
    const {loading, error, data} = useQuery(PICKS_QUERY);
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);


    return <div>
        {loading ? "Loading" : error ? "Error" : !data ? "derp" :
            [
                userCells(data),
                gameCells(data),
                spreadCells(data),
                pickCells(data, sendData),
                resultCells(data),
                totalCells(data)
            ]
        }
    </div>

};

export default PicksGrid
