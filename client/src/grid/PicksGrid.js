import React, {useEffect} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import PickCell from "./PickCell";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";

function userCells(users) {
    return !users ? undefined :
        users.map((user, index) => {
            return <div className="name-cell" key={index}>{user.name}</div>
        });
}

function totalCells(users) {
    return !users ? undefined :
        users.map((user, index) => {
            return <div className="total-cell" key={index}>{user.total}</div>
        });
}

function gameCells(games) {
    return !games ? undefined :
        games.map((game, index) => {
            return <div className="game-cell" key={index}>{game.name}</div>
        });
}

function spreadCells(games) {
    return !games ? undefined :
        games.map((game, index) => {
            return <div className="spread-cell" key={index}>{game.spread}</div>
        });
}

function resultCells(games) {
    return !games ? undefined :
        games.map((game, index) => {
            return <div className="result-cell" key={index}>{game.result}</div>
        });
}


function pickCells(data, sendData) {
    return (!data.users || !data.games) ? undefined :
        data.users.map((user, index1) => {
            return data.games.map((game, index2) => {
                let pickSet = getPicksForUser(data.userPicks, user.name);
                let thisPick = getPickByGame(pickSet, game.name);

                const sendDataCallback = (event, updatedPick) => {
                    sendData({
                        variables: {
                            name: user.name,
                            week: "0",
                            game: game.name,
                            pick: updatedPick,
                        }
                    });
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

export function getPicksForUser(passedPicks, userName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pickSet => pickSet.user.name === userName)[0];

    return firstMatchingPick ? firstMatchingPick.picks : null
}

const PicksGrid = () => {
    const {loading, error, data, refetch} = useQuery(PICKS_QUERY, {variables: {week: "0"}, pollInterval: 600000});
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);


    useEffect(() => {
        let webSocket = new WebSocket(buildWebsocketUri());
        webSocket.onopen = () => {
            refetch().catch(err => {
                console.warn(`Refetch failed ${err}`)
            })
        };

        webSocket.onmessage = () => {
            refetch().catch(err => {
                console.warn(`Refetch failed ${err}`)
            })
        };
        return () => {
            if (webSocket.readyState === WebSocket.OPEN) {
                webSocket.close()
            } else {
                webSocket.onopen = () => {
                    webSocket.close()
                }
            }
        }
    });

    return <div>
        {loading ? "Loading" : error ? "Error" : !data ? "derp" :
            [
                userCells(data.users),
                gameCells(data.games),
                spreadCells(data.games),
                pickCells(data, sendData),
                resultCells(data.games),
                totalCells(data.users),
            ]
        }
    </div>

};


export default PicksGrid
