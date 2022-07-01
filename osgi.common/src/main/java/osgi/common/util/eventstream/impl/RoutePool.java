package osgi.common.util.eventstream.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import osgi.common.util.eventstream.api.Event;
import osgi.common.util.eventstream.api.IEventConsumer;
import osgi.common.util.eventstream.api.IPlugin;

public class RoutePool implements IPlugin, Closeable {

    private IEventConsumer nextConsumer;

    private final List<ExecutorService> executors;

    public RoutePool() {
        List<LinkedBlockingDeque<Runnable>> queueList = IntStream.range(0, 10).mapToObj((i) -> {
            return new LinkedBlockingDeque<Runnable>(3000);
        }).collect(Collectors.toList());
        List<ThreadPoolExecutor> executorList = queueList.stream()
                .map((queue) -> new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue))
                .collect(Collectors.toList());
        this.executors = new CopyOnWriteArrayList<ExecutorService>(executorList);
    }

    @Override
    public void setNext(IEventConsumer consumer) {
        this.nextConsumer = consumer;

    }

    @Override
    public void close() throws IOException {
        executors.forEach(ExecutorService::shutdown);
    }

    @Override
    public Event consume(Event event) {
        int threadIdx = event.getClientId() % executors.size();
        final ExecutorService executor = executors.get(threadIdx);
        executor.submit(() -> nextConsumer.consume(event));
        return event;
    }

}
