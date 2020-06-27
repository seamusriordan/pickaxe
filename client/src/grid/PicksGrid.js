import React, {useEffect, useState} from "react";
import {useMutation, useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import PickCells from "./PickCells";
import LinearCells from "./LinearCells"
import ChangeWeek from "../ChangeWeek";
import {Leaderboard} from "../leaderboard/Leaderboard";

function destructureUserData(users) {
    return {
        names: users?.map(user => user.name),
    };
}
function destructureTotalData(totals) {
    return {
        total: totals?.map(total => total.total),
    };
}

function destructureGameData(games) {
    return {
        names: games?.map(game => game.name),
        spreads: games?.map(game => game.spread),
        results: games?.map(game => game.result)
    };
}

function indexIsPastEndOfData(index, data) {
    return index >= data.weeks.length - 1;
}

function generateAdvanceWeekCallback(data, currentWeek, updateWeek, refetch) {
    return () => {
        const index = data.weeks.findIndex(week => week.name === currentWeek)
        if (indexIsPastEndOfData(index, data)) {
            return;
        }
        const nextWeek = data.weeks[index + 1].name;
        updateWeek(nextWeek);
        refetch({week: nextWeek}).catch(err => {
            console.warn(`Refetch failed ${err}`)
        });
    };
}

function generateRewindWeekCallback(data, currentWeek, updateWeek, refetch) {
    return () => {
        const index = data.weeks.findIndex(week => week.name === currentWeek)
        if (index === 0) {
            return;
        }
        const previousWeek = data.weeks[index - 1].name;
        updateWeek(previousWeek);
        refetch({week: previousWeek}).catch(err => {
            console.warn(`Refetch failed ${err}`)
        });
    };
}

function generateWebsocketOnOpenCallback(refetch) {
    return () => {
        refetch().catch(err => {
            console.warn(`Refetch failed ${err}`)
        })
    };
}

function generatedWebsocketOnMessageCallback(refetch) {
    return () => {
        refetch().catch(err => {
            console.warn(`Refetch failed ${err}`)
        })
    };
}

function generateUseEffectCleanupCallback(webSocket) {
    return () => {
        if (webSocket.readyState === WebSocket.OPEN) {
            webSocket.close()
        } else {
            webSocket.onopen = () => {
                webSocket.close()
            }
        }
    };
}

const PicksGrid = props => {
    const {defaultWeek} = props;
    const [currentWeek, updateWeek] = useState(defaultWeek);
    const {error, data, refetch} = useQuery(PICKS_QUERY, {
        variables: {week: defaultWeek},
        pollInterval: 150000
    });
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);

    useEffect(() => {
        let webSocket = new WebSocket(buildWebsocketUri());
        webSocket.onopen = generateWebsocketOnOpenCallback(refetch);
        webSocket.onmessage = generatedWebsocketOnMessageCallback(refetch);
        return generateUseEffectCleanupCallback(webSocket)
    });

    const users = destructureUserData(data?.users);
    const games = destructureGameData(data?.games);
    const totals = destructureTotalData(data?.userTotals);

    const advanceWeek = generateAdvanceWeekCallback(data, currentWeek, updateWeek, refetch);
    const rewindWeek = generateRewindWeekCallback(data, currentWeek, updateWeek, refetch);

    return <div>
        { error ? "Error" : !data ? "Waiting for data..." :
            [
                <Leaderboard key="leaderboard" data={data.leaders}/>,
                <ChangeWeek key="change-week" id="change-week" week={currentWeek} forward={advanceWeek} back={rewindWeek}/>,
                <LinearCells key="name-cells" items={users.names} name="name"/>,
                <LinearCells key="game-cells" id="game-cells" items={games.names} name="game"/>,
                <LinearCells key="spread-cells" items={games.spreads} name="spread"/>,
                <PickCells key="pick-cells" id="pick-cells" data={data} sendData={sendData} currentWeek={currentWeek}/>,
                <LinearCells key="result-cells" items={games.results} name="result"/>,
                <LinearCells key="total-cells" items={totals.total} name="total"/>
            ]
        }
    </div>

};

export default PicksGrid
