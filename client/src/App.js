import React from 'react';
import PicksGrid from "./PicksGrid";
import {ApolloProvider} from '@apollo/react-hooks'
import ApolloClient from "apollo-boost";


export function graphqlServer() {
    return process.env.REACT_APP_GRAPHQL_SERVER ?
        process.env.REACT_APP_GRAPHQL_SERVER :
        "localhost";
}

export function graphqlPort() {
    return process.env.REACT_APP_GRAPHQL_PORT ?
        process.env.REACT_APP_GRAPHQL_PORT :
        "8080";
}

export function graphqlProtocol() {
    return process.env.REACT_APP_GRAPHQL_SECUREHTTP ?
        "https" :
        "http";
}

const client = new ApolloClient({
    uri: graphqlProtocol() + '://' +
        graphqlServer() + ':' + graphqlPort() +
        '/pickaxe/graphql',
});

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <a
                    className="App-link"
                    href="https://www.possum.best"
                    target="_blank"
                    rel="noopener noreferrer"
                >Make a pick</a>

                Also we have newly deployed
            </header>
            <ApolloProvider client={client}><PicksGrid/></ApolloProvider>
        </div>
    );
}

export default App;
