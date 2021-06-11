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

function generateWebsocketCleanup(webSocket) {
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
    const [selectedWeek, updateWeek] = useState(defaultWeek);
    const [loadedData, updateLoadedData] = useState(null);

    const {error, data, refetch} = useQuery(PICKS_QUERY, {
        variables: {week: selectedWeek},
        pollInterval: 150000
    });


    useEffect(() => {
        if(!!data) {
            updateLoadedData(data);
        }
    }, [data]);


    useEffect(() => {
        let webSocket = new WebSocket(buildWebsocketUri());
        webSocket.onopen = generateWebsocketOnOpenCallback(refetch);
        webSocket.onmessage = generatedWebsocketOnMessageCallback(refetch);
        return generateWebsocketCleanup(webSocket)
    });


    const advanceWeek = generateAdvanceWeekCallback(loadedData, selectedWeek, updateWeek, refetch);
    const rewindWeek = generateRewindWeekCallback(loadedData, selectedWeek, updateWeek, refetch);


    return <div className="weekly-view-app">
        {error ? "Error" : !loadedData ? "Waiting for data..." :
            [
                <Leaderboard key="leaderboard" data={loadedData.leaders}/>,
                <ChangeWeek key="change-week"
                            id="change-week"
                            week={selectedWeek} forward={advanceWeek}
                            back={rewindWeek}/>,
                <WeeklyGamesGrid
                    key="weekly-games-grid"
                    data-testid="weekly-games-grid"
                    currentWeek={selectedWeek}
                    users={loadedData?.users}
                    games={loadedData?.games}
                    totals={loadedData?.userTotals}
                    userPicks={loadedData.userPicks}
                />
            ]
        }
    </div>

};

export default WeeklyViewApp
