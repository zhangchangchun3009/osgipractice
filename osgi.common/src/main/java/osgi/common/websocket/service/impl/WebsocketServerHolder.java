package osgi.common.websocket.service.impl;

import java.util.ArrayList;

import osgi.common.websocket.service.AppWebSocketServer;

public class WebsocketServerHolder {
    private static ArrayList<AppWebSocketServer> serverInstances = new ArrayList<>();

    public static void add(AppWebSocketServer instance) {
        serverInstances.add(instance);
    }

    public static ArrayList<AppWebSocketServer> get() {
        return serverInstances;
    }
}
