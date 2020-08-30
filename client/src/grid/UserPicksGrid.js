import PickCell from "./PickCell";
import React from "react";
import UserPickColumn from "./UserPickColumn";

const UserPicksGrid = props => {
    const {data, sendData, currentWeek} = props;
    return (!data.users || !data.games) ? undefined :
        data.users.map((user, userIndex) => {
            return <div className='grid-column' key={`grid-column-${userIndex}`}>
                <UserPickColumn
                    user={user}
                    userIndex={userIndex}
                    games={data.games}
                    userPicks={data.userPicks}
                    sendData={sendData}
                    currentWeek={currentWeek}
                /></div>
        });
}

export default UserPicksGrid

export function getPickByGame(passedPicks, gameName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pick => pick["game"] === gameName)[0];

    return firstMatchingPick ? firstMatchingPick["pick"] : null
}

export function getPicksForUser(passedPicks, userName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pickSet => pickSet.user.name === userName)[0];

    return firstMatchingPick ? firstMatchingPick.picks : null
}

