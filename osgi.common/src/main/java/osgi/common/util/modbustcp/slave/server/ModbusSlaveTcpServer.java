package osgi.common.util.modbustcp.slave.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.modbus.codec.Modbus;
import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;

import osgi.common.util.modbustcp.slave.db.ModbusPoolDBMS;
import osgi.common.util.modbustcp.slave.requesthandler.HoldingRegisterRequestHandler;

/**
 * The Class ModbusSlaveTcpServer.
 *
 * @author zcc
 * @since 2021年12月13日
 */
public class ModbusSlaveTcpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusSlaveTcpServer.class);

    private ModbusTcpSlave server;

    private final String bindHostAddress;

    private final int port;

    private final short serverId;

    private final List<String> unitList = new ArrayList<String>();

    public int getPort() {
        return port;
    }

    public short getServerId() {
        return serverId;
    }

    public List<String> getUnitList() {
        return Collections.unmodifiableList(unitList);
    }

    public int getUnitInfo(String unitId) {
        return ModbusPoolDBMS.getCapacity(unitId);
    }

    public ModbusSlaveTcpServer() {
        this("0.0.0.0", 502, (short) 1);
        unitList.add(createUnitInstance(1, 256));
    }

    public static class Builder {
        private String bindHostAddress;

        private int port;

        private short serverId;

        private List<String> tempUnitList = new ArrayList<String>();

        public Builder bindHostAddress(String bindHostAddress) {
            this.bindHostAddress = bindHostAddress;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder serverId(short serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder addUnit(int unitId, int poolSize) {
            if (tempUnitList.contains(String.valueOf(unitId))) {
                throw new IllegalArgumentException("unitId " + unitId + "doesn't exist");
            }
            tempUnitList.add(createUnitInstance(unitId, poolSize));
            return this;
        }

        public ModbusSlaveTcpServer build() {
            ModbusSlaveTcpServer slave = new ModbusSlaveTcpServer(bindHostAddress, port, serverId);
            if (tempUnitList.isEmpty()) {
                tempUnitList.add(createUnitInstance(1, 256));
            }
            slave.unitList.addAll(tempUnitList);
            return slave;
        }

    }

    private ModbusSlaveTcpServer(String bindHostAddress, int port, short serverId) {
        this.bindHostAddress = bindHostAddress;
        this.port = port;
        this.serverId = serverId;
    }

    /**
     * Creates the unit instance.
     *
     * @param poolSize the pool size
     * @return the unitId
     */
    private static String createUnitInstance(int unitId, int poolSize) {
        String unitIdStr = String.valueOf(unitId);
        ModbusPoolDBMS.createInstance(unitIdStr, poolSize);
        return unitIdStr;
    }

    public void start() {
        for (String unitId : unitList) {
            ModbusPoolDBMS.start(unitId);
        }
        ModbusTcpSlaveConfig config = new ModbusTcpSlaveConfig.Builder().setInstanceId(String.valueOf(serverId))
                .build();
        server = new ModbusTcpSlave(config);
        server.setRequestHandler(new HoldingRegisterRequestHandler());
        CompletableFuture<ModbusTcpSlave> future = server.bind(bindHostAddress, port);
        try {
            future.get();
            LOGGER.info("modbus slave service start success, port:" + port);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("ModbusSlaveTcpServer start failed", e);
        }
    }

    public void shutdown() {
        for (String unitId : unitList) {
            ModbusPoolDBMS.stop(unitId);
        }
        server.shutdown();
        Modbus.releaseSharedResources();
    }

}
