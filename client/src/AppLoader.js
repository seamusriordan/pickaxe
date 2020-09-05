import React from "react";
import WeeklyViewApp from "./WeeklyViewApp";
import {useQuery} from "@apollo/react-hooks";
import {WEEKS_QUERY} from "./graphqlQueries";

const AppLoader = () => {
    const {loading, error, data} = useQuery(WEEKS_QUERY)

    if(error?.networkError?.statusCode === 401){
        window.location.href = "/pickaxe/authorize"
    }

    return <div>{
            loading ? "Loading App" :
            error ? "graphQL query failed" :
            <WeeklyViewApp id="weekly-view-app" defaultWeek={data.currentWeek.name}/>
    }</div>;
}

export default AppLoader