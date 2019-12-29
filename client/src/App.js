import React from 'react';
import PicksGrid from "./PicksGrid";
import {ApolloProvider } from '@apollo/react-hooks'
import ApolloClient from "apollo-boost";

const client = new ApolloClient({
    uri: 'https://localhost:8080/graphql',
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
