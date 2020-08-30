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

const WeeklyGamesGrid = props => {
    const [sendData] = useMutation(UPDATE_PICKS_MUTATION);

    const userNames = props.users?.map(user => user.name);
    const gameNames = props.games?.map(game => game.name);
    const gameSpreads = props.games?.map(game => game.spread);
    const gameResults = props.games?.map(game => game.result);

    const totalValues = props.totals?.map(totalData => totalData.total);

    const sendDataForWeek = (userName, gameName, updatedPick) => sendData({
        variables: {
            name: userName,
            week: props.currentWeek,
            game: gameName,
            pick: updatedPick,
        }
    });


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
        <div className='grid-column' key="grid-games">
            <LinearCells key="game-cells" id="game-cells"
                         items={gameNames} name="game"
            />
        </div>,
        <div className='grid-column' key="grid-spreads">
            <LinearCells key="spread-cells"
                         items={gameSpreads} name="spread"
            />
        </div>,
        <div className='grid-column' key="grid-picks">
            <UserPicksGrid id="user-picks-grid" key="user-picks-grid"
                           users={props.users}
                           games={props.games}
                           userPicks={props.userPicks}
                           sendData={sendDataForWeek}
            />
        </div>,
        <div className='grid-column' key="grid-results">
            <LinearCells key="result-cells"
                         items={gameResults} name="result"
            />
        </div>,
        <div className='grid-column' key="grid-right-padding">
            <LinearCells key="right-padding-cells"
                         items={blankCells(gameResults.length)} name="right-padding"
            />
        </div>,
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