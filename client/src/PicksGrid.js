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

function gameCells(data) {
    return !data.games? undefined :
    data.games.map((game, index) => {
        return <div className="game-cell" key={index}>{game.name}</div>
    });
}

const PicksGrid = () => {
    const {loading, error, data} = useQuery(USER_QUERY);

    return <div>
        {loading ? "Loading" : error ? "Error" : !data ? "derp" :
            [userCells(data), gameCells(data)]
        }
    </div>

};

export default PicksGrid
