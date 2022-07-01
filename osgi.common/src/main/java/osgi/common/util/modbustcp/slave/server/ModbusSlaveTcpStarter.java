package osgi.common.util.modbustcp.slave.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class ModbusSlaveTcpStarter.
 * @author zcc
 * @since 2021年12月13日
 */
public class ModbusSlaveTcpStarter {

    private String ip;

    private int port;

    private short serverId;

    public void process() {
        List<Map<String, Object>> plcList = new ArrayList<Map<String, Object>>(); //master list from configuration,may from db
        ModbusSlaveTcpServer.Builder builder = new ModbusSlaveTcpServer.Builder();
        builder.bindHostAddress(ip).port(port).serverId(serverId);
        for (Map<String, Object> plc : plcList) {
            int poolSize = (int) plc.get("poolSize");
            int unitId = (int) plc.get("unitId");
            builder.addUnit(unitId, poolSize);
        }
        ModbusSlaveTcpServer slave = builder.build();
        ModbusSlaveInstanceRegister.regist(serverId, slave);
        slave.start();
    }

    public int getOrder() {
        return 0;
    }

}
