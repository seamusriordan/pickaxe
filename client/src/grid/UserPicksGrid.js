import React from "react";
import UserPickColumn from "./UserPickColumn";

const UserPicksGrid = props => {
    const {data, sendData} = props;
    return (!data.users || !data.games) ? undefined :
        data.users.map(user => {
            return <UserPickColumn
                className='grid-column' key={`grid-column-${user.name}`}
                user={user}
                games={data.games}
                pickSet={getPicksForUser(data.userPicks, user.name)}
                sendData={sendData}
            />
        });
}

export default UserPicksGrid


export function getPicksForUser(passedPicks, userName) {
    if (!passedPicks || passedPicks.size === 0) return null;
    const firstMatchingPick = passedPicks.filter(pickSet => pickSet.user.name === userName)[0];

    return firstMatchingPick ? firstMatchingPick.picks : null
}

