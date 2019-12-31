import React from "react";
import {useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";

const USER_QUERY = gql`query Query { users { name }}`;

function userCells(data) {
    return !data.users? undefined :
    data.users.map((user, index) => {
        return <div className="name-cell" key={index}>{user.name}</div>
    });
}

function totalCells(data) {
    return !data.users? undefined :
    data.users.map((user, index) => {
        return <div className="total-cell" key={index}>{user.total}</div>
    });
}

function gameCells(data) {
    return !data.games? undefined :
    data.games.map((game, index) => {
        return <div className="game-cell" key={index}>{game.name}</div>
    });
}

function spreadCells(data) {
    return !data.games? undefined :
    data.games.map((game, index) => {
        return <div className="spread-cell" key={index}>{game.spread}</div>
    });
}

function resultCells(data) {
    return !data.games? undefined :
    data.games.map((game, index) => {
        return <div className="result-cell" key={index}>{game.result}</div>
    });
}

function pickCells(data) {
    return (!data.users || !data.games)? undefined :
       data.users.map((user, index1) => {
           return data.games.map((game, index2) => {
               return <div className="pick-cell" id={user.name + '-' + game.name} key={index1+'-'+index2}>{getPickByGame(user.picks, game.name)}</div>
           });
       });
}

export function getPickByGame(picks, game){
    return picks.filter(pick => pick["game"] === game)[0]["pick"]
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
