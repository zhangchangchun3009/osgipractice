package osgi.common.websocket.service.impl;

import osgi.common.websocket.service.ScmWebSocketServer;

public class WebsocketServerStarter {

    public void process() {
        ScmWebSocketServer instance = new ScmWebSocketServer(9005);
        instance.start();
        WebsocketServerHolder.add(instance);
    }

    public int getOrder() {
        return 1;
    }

}
