package osgi.common.util.eventstream.api;

public interface IEventConsumer {
    Event consume(Event event);
}
