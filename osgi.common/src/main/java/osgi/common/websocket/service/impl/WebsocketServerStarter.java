package osgi.common.websocket.service.impl;

import javax.inject.Named;

import osgi.common.Activator;
import osgi.common.IAfterStartUpHandle;
import osgi.common.websocket.service.AppWebSocketServer;

@Named
public class WebsocketServerStarter implements IAfterStartUpHandle {

    @Override
    public void process() {
        AppWebSocketServer instance = new AppWebSocketServer(9005, Activator.BUNDLE_NAME);
        instance.start();
        WebsocketServerHolder.add(instance);
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
