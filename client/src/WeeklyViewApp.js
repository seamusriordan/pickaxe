import React, {useEffect, useState} from "react";
import './WeeklyViewApp.css'
import {useQuery} from "@apollo/react-hooks";
import {buildWebsocketUri} from "./helpers";
import {PICKS_QUERY} from "./graphqlQueries";
import ChangeWeek from "./ChangeWeek";
import {Leaderboard} from "./leaderboard/Leaderboard";
import WeeklyGamesGrid from "./grid/WeeklyGamesGrid";

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


const WeeklyViewApp = props => {
    const {defaultWeek} = props;
    const [currentWeek, updateWeek] = useState(defaultWeek);
    const {error, data, refetch} = useQuery(PICKS_QUERY, {
        variables: {week: defaultWeek},
        pollInterval: 150000
    });


    useEffect(() => {
        let webSocket = new WebSocket(buildWebsocketUri());
        webSocket.onopen = generateWebsocketOnOpenCallback(refetch);
        webSocket.onmessage = generatedWebsocketOnMessageCallback(refetch);
        return generateUseEffectCleanupCallback(webSocket)
    });


    const advanceWeek = generateAdvanceWeekCallback(data, currentWeek, updateWeek, refetch);
    const rewindWeek = generateRewindWeekCallback(data, currentWeek, updateWeek, refetch);


    return <div className="weekly-view-app">
        {error ? "Error" : !data ? "Waiting for data..." :
            [
                <Leaderboard key="leaderboard" data={data.leaders}/>,
                <ChangeWeek key="change-week"
                            id="change-week"
                            week={currentWeek} forward={advanceWeek}
                            back={rewindWeek}/>,
                <WeeklyGamesGrid
                    key="weekly-games-grid"
                    currentWeek={currentWeek}
                    users={data?.users}
                    games={data?.games}
                    totals={data?.userTotals}
                    userPicks={data.userPicks}
                />
            ]
        }
    </div>

};

export default WeeklyViewApp
