import React from "react";
import WeeklyGamesGrid from "./grid/WeeklyGamesGrid";
import {useQuery} from "@apollo/react-hooks";
import {WEEKS_QUERY} from "./graphqlQueries";

const PicksLoader = () => {
    const {loading, error, data} = useQuery(WEEKS_QUERY)

    return <div>{
            loading ? "Loading App" :
            error ? "Something has gone wrong" :
            <WeeklyGamesGrid id="picks-grid" defaultWeek={data.currentWeek.name}/>
    }</div>;
}

export default PicksLoader