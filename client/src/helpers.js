export function getWebsocketProtocol() {
    return window.location.protocol === 'https:'?
        "wss:" :
        "ws:";
}

function getHostFromWindowLocation() {
    return process.env.REACT_APP_PORT ?
        window.location.hostname + ':' + process.env.REACT_APP_PORT :
        window.location.host;
}

export function buildWebsocketUri() {
    return  `${getWebsocketProtocol()}//${(getHostFromWindowLocation())}/pickaxe/updateNotification`;
}

export function buildGraphqlUri() {
    return `${window.location.protocol}//${(getHostFromWindowLocation())}/pickaxe/graphql`;
}