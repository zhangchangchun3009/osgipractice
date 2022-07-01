
package osgi.common.util.modbustcp.slave.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class ModbusSlaveInstanceRegister.
 *
 * @author zcc
 * @since 2021年12月13日
 */
public class ModbusSlaveInstanceRegister {

    /** The instance map. */
    private static ConcurrentHashMap<Integer, ModbusSlaveTcpServer> instanceMap = new ConcurrentHashMap<Integer, ModbusSlaveTcpServer>();

    public static Collection<ModbusSlaveTcpServer> getInstances() {
        return instanceMap.values();
    }

    /**
     * Gets the single instance of ModbusSlaveInstanceRegister.
     *
     * @param unitId the unit id
     * @return single instance of ModbusSlaveInstanceRegister
     */
    public static ModbusSlaveTcpServer getInstance(int serverId) {
        return instanceMap.get(Integer.valueOf(serverId));
    }

    /**
     * Regist.
     *
     * @param unitId the unit id
     * @param instance the instance
     */
    public static void regist(int serverId, ModbusSlaveTcpServer instance) {
        instanceMap.putIfAbsent(Integer.valueOf(serverId), instance);
    }
}
