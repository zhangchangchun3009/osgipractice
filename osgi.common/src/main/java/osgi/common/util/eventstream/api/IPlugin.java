package osgi.common.util.eventstream.api;

public interface IPlugin extends IEventConsumer {

    void setNext(IEventConsumer nextConsumer);

}
