import LinearCells from "./LinearCells";
import UserPicksGrid from "./UserPicksGrid";
import React from "react";
import {useMutation} from "@apollo/react-hooks";
import {UPDATE_PICKS_MUTATION} from "../graphqlQueries";
import "./WeeklyGamesGrid.css"

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
            <div className='grid__cell grid__cell--name grid__cell--top-padding'/>
            <div className='grid__cell grid__cell--name grid__cell--top-padding'/>
            <LinearCells key="grid__cell--names"
                         items={blankCells(userNames.length)} name="top-padding"
            />
            <div className='grid__cell grid__cell--name grid__cell--top-padding'/>
        </div>,
        <div key="grid-names">
            <div className='grid__cell grid__cell--name grid__cell--border-bottom'/>
            <div className='grid__cell grid__cell--name grid__cell--border-bottom'>Spread</div>
            <LinearCells key="grid__cell--names"
                         items={userNames} name="name"
            />
            <div className='grid__cell grid__cell--name grid__cell--border'>Result</div>
        </div>,
        <LinearCells key="game-cells"
                     id="game-cells"
                     items={gameNames} name="game"
        />,
        <LinearCells key="spread-cells"
                     className='grid__column'
                     items={gameSpreads} name="spread"
        />,
        <UserPicksGrid id="user-picks-grid"
                       key="user-picks-grid"
                       users={props.users}
                       games={props.games}
                       userPicks={props.userPicks}
                       sendData={composeSendDataForWeek(props.currentWeek, sendData)}
        />,
        <LinearCells key="result-cells"
                     items={gameResults} name="result"
        />,
        <LinearCells key="right-padding-cells"
                     items={blankCells(gameResults.length)} name="right-padding"
        />,
        <div key="grid-totals">
            <div className='grid__cell grid__cell--total'/>
            <div className='grid__cell grid__cell--total'/>
            <LinearCells key="total-cells"
                         items={totalValues} name="total"
            />
            <div className='grid__cell grid__cell--total grid__cell--border-left'/>
        </div>
    ]
}

export default WeeklyGamesGrid