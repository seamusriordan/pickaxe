export function getWebsocketProtocol() {
    return window.location.protocol === 'https'?
        "wss" :
        "ws";
}

export function buildWebsocketUri() {
    return getWebsocketProtocol() + '://' +
        window.location.host +
        '/pickaxe/updateNotification';
}

export function buildGraphqlUri() {
    return window.location.protocol + '://' +
        window.location.host +
        '/pickaxe/graphql';
}