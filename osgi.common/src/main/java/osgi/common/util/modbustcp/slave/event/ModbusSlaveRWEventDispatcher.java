
package osgi.common.util.modbustcp.slave.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ModbusSlaveRWEventDispatcher.
 *
 * @author zhangchangchun
 * @since 2021年12月25日
 */
public class ModbusSlaveRWEventDispatcher {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusSlaveRWEventDispatcher.class);

    private static HashMap<IModbusSlaveRWEventHandler, List<ModbusSlaveEvent>> handlerRegister = new HashMap<>();

    public static void registEvent(ModbusSlaveEvent event, IModbusSlaveRWEventHandler handler) {
        List<ModbusSlaveEvent> list = handlerRegister.get(handler);
        if (list == null) {
            list = new ArrayList<ModbusSlaveEvent>();
            list.add(event);
            handlerRegister.put(handler, list);
            return;
        }
        if (list.contains(event)) {
            return;
        } else {
            list.add(event);
        }
    }

    public static void dispatch(ModbusSlaveEvent event) {
        if (handlerRegister.size() < 1) {
            return;
        }
        for (IModbusSlaveRWEventHandler handler : handlerRegister.keySet()) {
            List<ModbusSlaveEvent> list = handlerRegister.get(handler);
            boolean trigger = false;
            for (ModbusSlaveEvent regEvent : list) {
                if (regEvent.getEventType() == event.getEventType() && regEvent.getUnitId() == event.getUnitId()
                        && regEvent.getAddress() >= event.getAddress() && (regEvent.getAddress()
                                + regEvent.getQuantity()) <= (event.getAddress() + event.getQuantity())) {
                    trigger = true;
                    break;
                }
            }
            if (trigger) {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    LOGGER.error("handle ModbusSlaveEvent err,", e);
                }
            }
        }

    }
}
