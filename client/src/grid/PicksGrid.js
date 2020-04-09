import React, {useEffect} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import PickCells from "./PickCells";
import RowOrColumnCells from "./RowOrColumnCells"

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
                <RowOrColumnCells key="name-cells" items={data.users?.map(user => user.name)} name="name"/>,
                <RowOrColumnCells key="game-cells" items={data.games?.map(game => game.name)} name="game"/>,
                <RowOrColumnCells key="spread-cells" items={data.games?.map(game => game.spread)} name="spread"/>,
                <PickCells key="pick-cells" data={data} sendData={sendData}/>,
                <RowOrColumnCells key="result-cells" items={data.games?.map(game => game.result)} name="result"/>,
                <RowOrColumnCells key="total-cells" items={data.users?.map(user => user.total)} name="total"/>
            ]
        }
    </div>

};


export default PicksGrid
