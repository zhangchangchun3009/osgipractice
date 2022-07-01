package osgi.common.websocket.service.impl;

import java.util.ArrayList;

import osgi.common.websocket.service.ScmWebSocketServer;

public class WebsocketServerReleaser {

    public void process() {
        ArrayList<ScmWebSocketServer> instances = WebsocketServerHolder.get();
        if (!instances.isEmpty()) {
            for (ScmWebSocketServer instance : instances) {
                try {
                    instance.stop(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getOrder() {
        return 0;
    }

}
