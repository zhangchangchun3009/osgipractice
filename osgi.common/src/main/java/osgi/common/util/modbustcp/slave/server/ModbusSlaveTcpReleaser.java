package osgi.common.util.modbustcp.slave.server;

public class ModbusSlaveTcpReleaser {

    public void process() {
        for (ModbusSlaveTcpServer slave : ModbusSlaveInstanceRegister.getInstances()) {
            slave.shutdown();
        }
    }

    public int getOrder() {
        return 0;
    }

}
