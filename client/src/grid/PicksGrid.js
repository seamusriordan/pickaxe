import React, {useEffect} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import {PickCells} from "./PickCells";
import {RowOrColumnCells} from "./RowOrColumnCells";



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
                RowOrColumnCells(data.users?.map(user => user.name), "name"),
                RowOrColumnCells(data.games?.map(game => game.name), "game"),
                RowOrColumnCells(data.games?.map(game => game.spread), "spread"),
                PickCells(data, sendData),
                RowOrColumnCells(data.games?.map(game => game.result), "result"),
                RowOrColumnCells(data.users?.map(user => user.total), "total"),
            ]
        }
    </div>

};


export default PicksGrid
