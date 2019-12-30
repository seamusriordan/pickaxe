import React from 'react';
import PicksGrid from "./PicksGrid";
import {ApolloProvider} from '@apollo/react-hooks'
import ApolloClient from "apollo-client";
import { InMemoryCache } from 'apollo-cache-inmemory';
import { HttpLink } from 'apollo-link-http';

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
    return process.env.REACT_APP_GRAPHQL_HTTPS ?
        "https" :
        "http";
}


export function serverUri() {
    return graphqlProtocol() + '://' +
        graphqlServer() + ':' + graphqlPort() +
        '/pickaxe/graphql';
}

const client = new ApolloClient({
    link: new HttpLink(
        {uri: serverUri()}),
    cache: new InMemoryCache(),
    connectToDevTools: true
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
