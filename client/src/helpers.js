export function getWebsocketHostname() {
    return process.env.REACT_APP_GRAPHQL_SERVER ?
        process.env.REACT_APP_GRAPHQL_SERVER :
        "localhost";
}

export function getWebsocketPort() {
    return process.env.REACT_APP_GRAPHQL_PORT ?
        process.env.REACT_APP_GRAPHQL_PORT :
        "8080";
}

export function getWebsocketProtocol() {
    return process.env.REACT_APP_GRAPHQL_HTTPS ?
        "wss" :
        "ws";
}

export function buildWebsocketUri() {
    return getWebsocketProtocol() + '://' +
        getWebsocketHostname() + ':' + getWebsocketPort() +
        '/pickaxe/updateNotification';
}

export function getGraphqlServer() {
    return process.env.REACT_APP_GRAPHQL_SERVER ?
        process.env.REACT_APP_GRAPHQL_SERVER :
        "localhost";
}

export function getGraphqlPort() {
    return process.env.REACT_APP_GRAPHQL_PORT ?
        process.env.REACT_APP_GRAPHQL_PORT :
        "8080";
}

export function getGraphqlProtocol() {
    return process.env.REACT_APP_GRAPHQL_HTTPS ?
        "https" :
        "http";
}

export function buildGraphqlUri() {
    return getGraphqlProtocol() + '://' +
        getGraphqlServer() + ':' + getGraphqlPort() +
        '/pickaxe/graphql';
}