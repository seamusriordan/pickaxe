import React from 'react';
import PicksGrid from "./PicksGrid";

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
            <div><PicksGrid/></div>
        </div>
    );
}

export default App;
