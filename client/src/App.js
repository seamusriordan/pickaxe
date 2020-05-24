import React from 'react';
import {ApolloProvider} from '@apollo/react-hooks'
import ApolloClient from "apollo-client";
import {InMemoryCache} from 'apollo-cache-inmemory';
import {HttpLink} from 'apollo-link-http';
import {buildGraphqlUri} from "./helpers";
import PicksLoader from "./PicksLoader";


export const apolloClient = new ApolloClient({
    link: new HttpLink(
        {uri: buildGraphqlUri()}),
    cache: new InMemoryCache(),
    // defaultOptions: {query: {fetchPolicy: 'no-cache'}, watchQuery: {fetchPolicy: 'no-cache'}},
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

                Also we have a new pipeline!
            </header>
            <ApolloProvider client={apolloClient}><PicksLoader/></ApolloProvider>
        </div>
    );
}

export default App;
