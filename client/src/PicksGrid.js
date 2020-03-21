import React, {useEffect} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";
import PickCell from "./PickCell";

const PICKS_QUERY = gql`
    query Query($week: Int) {
        users {
            name
        }

        userPicks(week: $week) {
            user { name }
            picks {
                game
                pick
            }
            total
        }

        games(week: $week) {
            week
            name
            spread
            result
        }
    }`;

const UPDATE_PICKS_MUTATION =
gql`
    mutation Mutation($name: String!, $week: Int!, $game: String!, $pick: String!) {
        updatePick(name: $name, userPick: { week: $week, game: $game, pick: $pick })
    }`;

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
                let pickSet = getPicksForUser(data.userPicks, user.name);
                let thisPick = getPickByGame(pickSet, game.name);

                const sendDataCallback = (event, updatedPick) => {
                    sendData({
                        variables: {
                            name: user.name,
                            week: 0,
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
    const {loading, error, data, refetch} = useQuery(PICKS_QUERY, {variables: {week: 0}, pollInterval: 600000});
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);


    useEffect(() => {
        let webSocket = new WebSocket(websocketUri());
        webSocket.onmessage = () => {
            refetch()
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
                userCells(data),
                gameCells(data),
                spreadCells(data),
                pickCells(data, sendData),
                resultCells(data),
                totalCells(data),
            ]
        }
    </div>

};

export function websocketServer() {
    return process.env.REACT_APP_GRAPHQL_SERVER ?
        process.env.REACT_APP_GRAPHQL_SERVER :
        "localhost";
}

export function websocketPort() {
    return process.env.REACT_APP_GRAPHQL_PORT ?
        process.env.REACT_APP_GRAPHQL_PORT :
        "8080";
}

export function websocketProtocol() {
    return process.env.REACT_APP_GRAPHQL_HTTPS ?
        "wss" :
        "ws";
}


export function websocketUri() {
    return websocketProtocol() + '://' +
        websocketServer() + ':' + websocketPort() +
        '/pickaxe/updateNotification';
}

export default PicksGrid
