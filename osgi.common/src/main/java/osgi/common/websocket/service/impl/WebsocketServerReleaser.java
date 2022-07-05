package osgi.common.websocket.service.impl;

import java.util.ArrayList;

import javax.inject.Named;

import osgi.common.IBeforeShutDownHandle;
import osgi.common.websocket.service.AppWebSocketServer;

@Named
public class WebsocketServerReleaser implements IBeforeShutDownHandle {

    @Override
    public void process() {
        ArrayList<AppWebSocketServer> instances = WebsocketServerHolder.get();
        if (!instances.isEmpty()) {
            for (AppWebSocketServer instance : instances) {
                try {
                    instance.stop(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
