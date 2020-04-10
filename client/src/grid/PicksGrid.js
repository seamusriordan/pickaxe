import React, {useEffect} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import PickCells from "./PickCells";
import LinearCells from "./LinearCells"

function destructureUserData(users) {
    return {
        names: users?.map(user => user.name),
        totals: users?.map(user => user.total)
    };
}

function destructureGameData(games) {
    return {
        names: games?.map(game => game.name),
        spreads: games?.map(game => game.spread),
        results: games?.map(game => game.result)
    };
}

const PicksGrid = props => {
    const {defaultWeek} = props;
    const {loading, error, data, refetch} = useQuery(PICKS_QUERY, {variables: {week: defaultWeek}, pollInterval: 600000});
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

    const users = destructureUserData(data?.users);
    const games = destructureGameData(data?.games);
    return <div>
        {loading ? "Loading" : error ? "Error" : !data ? "derp" :
            [
                <LinearCells key="name-cells" items={users.names} name="name"/>,
                <LinearCells key="game-cells" items={games.names} name="game"/>,
                <LinearCells key="spread-cells" items={games.spreads} name="spread"/>,
                <PickCells key="pick-cells" id="pick-cells"  data={data} sendData={sendData}/>,
                <LinearCells key="result-cells" items={games.results} name="result"/>,
                <LinearCells key="total-cells" items={users.totals} name="total"/>
            ]
        }
    </div>

};


export default PicksGrid
