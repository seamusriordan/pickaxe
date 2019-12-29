import React from "react";
import {useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";

const USER_QUERY = gql`{
    users {
        name
    }
}`;

const PicksGrid = () => {
    useQuery(USER_QUERY);

    return <div/>
};

export default PicksGrid
