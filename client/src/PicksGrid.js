import React from "react";
import {useQuery} from "@apollo/react-hooks";
import gql from "graphql-tag";

const USER_QUERY = gql`{
    users {
        name
    }
}`;

const PicksGrid = () => {
    const {loading, error, data} = useQuery(USER_QUERY);

    return <div>
        {loading ? "Loading" :
            error ? "Error" :
                !data ? "derp" :
            data.users.map((user, index) => {
            return <div className="namecell" key={index}>{user.name}</div>
        })}
    </div>

};

export default PicksGrid
