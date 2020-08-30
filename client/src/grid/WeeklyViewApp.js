import React, {useEffect, useState} from "react";
import './WeeklyViewApp.css'
import {useMutation, useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "../helpers";
import {PICKS_QUERY, UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import UserPicksGrid from "./UserPicksGrid";
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

function blankCells(size) {
    let blankArray = []
    for (let i = 0; i < size; i++) {
        blankArray.push("")
    }
    return blankArray
}

const WeeklyViewApp = props => {
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

    const sendDataForWeek = (userName, gameName, updatedPick) => sendData({
        variables: {
            name: userName,
            week: currentWeek,
            game: gameName,
            pick: updatedPick,
        }
    });
    return <div className='weekly-games-grid'>
        {error ? "Error" : !data ? "Waiting for data..." :
            [
                <Leaderboard key="leaderboard" data={data.leaders}/>,
                <div className="change-week" key="grid-change-week">
                    <ChangeWeek key="change-week" id="change-week"
                                week={currentWeek} forward={advanceWeek}
                                back={rewindWeek}/>
                </div>,
                <div key="grid-top-padding">
                    <div className='grid-cell name-cell top-padding-cell'/>
                    <div className='grid-cell name-cell top-padding-cell'/>
                    <LinearCells key="name-cells"
                                 items={blankCells(users.names.length)} name="top-padding"
                    />
                    <div className='grid-cell name-cell top-padding-cell'/>
                </div>,
                <div key="grid-names">
                    <div className='grid-cell name-cell border-bottom'/>
                    <div className='grid-cell name-cell border-bottom'>Spread</div>
                    <LinearCells key="name-cells"
                                 items={users.names} name="name"
                    />
                    <div className='grid-cell name-cell border-cell'>Result</div>
                </div>,
                <div className='grid-column' key="grid-games">
                    <LinearCells key="game-cells" id="game-cells"
                                 items={games.names} name="game"
                    />
                </div>,
                <div className='grid-column' key="grid-spreads">
                    <LinearCells key="spread-cells"
                                 items={games.spreads} name="spread"
                    />
                </div>,
                <div className='grid-column' key="grid-picks">
                    <UserPicksGrid id="user-picks-grid" key="user-picks-grid"
                                   data={data}
                                   sendData={sendDataForWeek}
                    />
                </div>,
                <div className='grid-column' key="grid-results">
                    <LinearCells key="result-cells"
                                 items={games.results} name="result"
                    />
                </div>,
                <div className='grid-column' key="grid-right-padding">
                    <LinearCells key="right-padding-cells"
                                 items={blankCells(games.results.length)} name="right-padding"
                    />
                </div>,
                <div key="grid-totals">
                    <div className='grid-cell name-cell'/>
                    <div className='grid-cell name-cell'/>
                    <LinearCells key="total-cells"
                                 items={totals.total} name="total"
                    />
                    <div className='grid-cell border-left total-cell'/>
                </div>
            ]
        }
    </div>

};

export default WeeklyViewApp
