import React from "react";
import WeeklyViewApp from "./grid/WeeklyViewApp";
import {useQuery} from "@apollo/react-hooks";
import {WEEKS_QUERY} from "./graphqlQueries";

const AppLoader = () => {
    const {loading, error, data} = useQuery(WEEKS_QUERY)

    return <div>{
            loading ? "Loading App" :
            error ? "Something has gone wrong" :
            <WeeklyViewApp id="weekly-view-app" defaultWeek={data.currentWeek.name}/>
    }</div>;
}

export default AppLoader