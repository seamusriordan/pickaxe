import React from "react";
import PicksGrid from "./grid/PicksGrid";
import {useQuery} from "@apollo/react-hooks";
import {WEEKS_QUERY} from "./graphqlQueries";

const PicksLoader = () => {
    const {loading, error, data} = useQuery(WEEKS_QUERY)

    return <div>{
            loading ? "Loading App" :
            error ? "Something has gone wrong" :
            <PicksGrid id="picks-grid" defaultWeek={data.currentWeek.week}/>
    }</div>;
}

export default PicksLoader