package osgi.common.util.eventstream.api;

import java.time.Instant;

public class Event {
    private final Instant createdInstant = Instant.now();

    private final int eventId;

    private final int clientId;

    public Event(int eventId, int clientId) {
        this.clientId = clientId;
        this.eventId = eventId;
    }

    public int getClientId() {
        return clientId;
    }

    public Instant getCreatedInstant() {
        return createdInstant;
    }

    public int getEventId() {
        return eventId;
    }

}
