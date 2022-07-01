package osgi.common.util.eventstream.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import osgi.common.util.eventstream.api.Event;
import osgi.common.util.eventstream.api.IClientHandler;
import osgi.common.util.eventstream.api.IEventStream;
import osgi.common.util.eventstream.api.IPlugin;

public class EventStream implements IEventStream {

    private static final int MAX_PLUGINS_SIZE = 16;

    private static final int INIT_PLUGIN_IDX = -1;

    private Event event;

    private final IPlugin[] plugins = new IPlugin[MAX_PLUGINS_SIZE];

    private int lastIdx = INIT_PLUGIN_IDX;

    private final AtomicBoolean init = new AtomicBoolean(false);

    @Override
    public EventStream receiveEvent(Event event) {
        this.event = event;
        return this;
    }

    @Override
    public EventStream addPlugin(IPlugin plugin) {
        try {
            plugins[++lastIdx] = plugin;
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("you have added too many plugins,limit " + MAX_PLUGINS_SIZE);
        }
        return this;
    }

    private void initPlugin(IClientHandler clientHandler) {
        if (!init.get()) {
            if (lastIdx > INIT_PLUGIN_IDX) {
                plugins[lastIdx].setNext(clientHandler);
                if (lastIdx >= INIT_PLUGIN_IDX + 2) {
                    for (int i = lastIdx - 1; i >= 0; i--) {
                        plugins[i].setNext(plugins[i + 1]);
                    }
                }
            }
            init.set(true);
        } else {
            if (lastIdx > INIT_PLUGIN_IDX) {
                plugins[lastIdx].setNext(clientHandler);
            }
        }
    }

    @Override
    public void consume(IClientHandler clientHandler) {
        initPlugin(clientHandler);
        if (lastIdx > INIT_PLUGIN_IDX) {
            plugins[lastIdx].consume(event);
        } else {
            clientHandler.consume(event);
        }

    }

    @Override
    public void close() {
        if (lastIdx == INIT_PLUGIN_IDX) {
            return;
        }
        for (int i = 0; i <= lastIdx; i++) {
            IPlugin plugin = plugins[i];
            if (plugin instanceof Closeable) {
                Closeable pluginC = (Closeable) plugin;
                try {
                    pluginC.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
