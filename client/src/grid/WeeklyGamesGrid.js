import LinearCells from "./LinearCells";
import UserPicksGrid from "./UserPicksGrid";
import React from "react";
import {useMutation} from "@apollo/react-hooks";
import {UPDATE_PICKS_MUTATION} from "../graphqlQueries";

function blankCells(size) {
    let blankArray = []
    for (let i = 0; i < size; i++) {
        blankArray.push("")
    }
    return blankArray
}

const composeSendDataForWeek = (week, sendData) => {
    return (userName, gameName, updatedPick) => sendData({
        variables: {
            name: userName,
            week: week,
            game: gameName,
            pick: updatedPick,
        }
    });
}

const WeeklyGamesGrid = props => {
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);

    const userNames = props.users?.map(user => user.name);

    const gameNames = props.games?.map(game => game.name);
    const gameSpreads = props.games?.map(game => game.spread);
    const gameResults = props.games?.map(game => game.result);

    const totalValues = props.totals?.map(totalData => totalData.total);

    return [
        <div key="grid-top-padding">
            <div className='grid-cell name-cell top-padding-cell'/>
            <div className='grid-cell name-cell top-padding-cell'/>
            <LinearCells key="name-cells"
                         items={blankCells(userNames.length)} name="top-padding"
            />
            <div className='grid-cell name-cell top-padding-cell'/>
        </div>,
        <div key="grid-names">
            <div className='grid-cell name-cell border-bottom'/>
            <div className='grid-cell name-cell border-bottom'>Spread</div>
            <LinearCells key="name-cells"
                         items={userNames} name="name"
            />
            <div className='grid-cell name-cell border-cell'>Result</div>
        </div>,
        <LinearCells key="game-cells"
                     id="game-cells"
                     className='grid-column'
                     items={gameNames} name="game"
        />,
        <LinearCells key="spread-cells"
                     className='grid-column'
                     items={gameSpreads} name="spread"
        />,
        <UserPicksGrid id="user-picks-grid"
                       key="user-picks-grid"
                       className='grid-column'
                       users={props.users}
                       games={props.games}
                       userPicks={props.userPicks}
                       sendData={composeSendDataForWeek(props.currentWeek, sendData)}
        />,
        <LinearCells key="result-cells"
                     className='grid-column'
                     items={gameResults} name="result"
        />,
        <LinearCells key="right-padding-cells"
                     className='grid-column'
                     items={blankCells(gameResults.length)} name="right-padding"
        />,
        <div key="grid-totals">
            <div className='grid-cell name-cell'/>
            <div className='grid-cell name-cell'/>
            <LinearCells key="total-cells"
                         items={totalValues} name="total"
            />
            <div className='grid-cell border-left total-cell'/>
        </div>
    ]
}

export default WeeklyGamesGrid