package osgi.common.util.eventstream.api;

import osgi.common.util.eventstream.impl.EventStream;

public interface IEventStream {
    EventStream receiveEvent(Event event);

    EventStream addPlugin(IPlugin plugin);

    void consume(IClientHandler clientHandler);

    void close();
}
