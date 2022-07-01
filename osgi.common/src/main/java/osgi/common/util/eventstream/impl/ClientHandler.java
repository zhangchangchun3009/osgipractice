package osgi.common.util.eventstream.impl;

import osgi.common.util.eventstream.api.Event;
import osgi.common.util.eventstream.api.IClientHandler;

public class ClientHandler implements IClientHandler {

    @Override
    public Event consume(Event event) {
        try {
            Sleeper.randomSleep(10, 1);
            System.out.println(event.getClientId());
            System.out.println(event.getEventId());
            System.out.println("**************************");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return event;
    }

}
