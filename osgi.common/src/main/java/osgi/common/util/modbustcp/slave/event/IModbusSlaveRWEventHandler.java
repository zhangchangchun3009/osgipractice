
package osgi.common.util.modbustcp.slave.event;

/**
 * The Interface IModbusSlaveRWEventHandler.
 *
 * @author zhangchangchun
 * @since 2021年12月25日
 */
public interface IModbusSlaveRWEventHandler {

    void handle(ModbusSlaveEvent event);

}
