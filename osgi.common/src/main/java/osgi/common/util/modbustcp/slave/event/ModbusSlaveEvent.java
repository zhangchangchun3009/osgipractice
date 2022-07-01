
package osgi.common.util.modbustcp.slave.event;

/**
 * The Class ModbusSlaveEvent.
 *
 * @author zhangchangchun
 * @since 2021年12月25日
 */
public final class ModbusSlaveEvent {

    private final ModbusSlaveEventType eventType;

    private final int unitId;

    private final int address;

    private final int quantity;

    private int hash;

    public ModbusSlaveEvent(ModbusSlaveEventType eventType, int unitId, int address, int quantity) {
        this.eventType = eventType;
        this.unitId = unitId;
        this.address = address;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        boolean res = false;
        if (this == obj) {
            return true;
        }
        if (obj instanceof ModbusSlaveEvent) {
            ModbusSlaveEvent compare = (ModbusSlaveEvent) obj;
            return eventType == compare.eventType && unitId == compare.unitId && address == compare.address
                    && quantity == compare.quantity;
        }
        return res;
    }

    @Override
    public int hashCode() {
        int value = hash;
        if (value == 0) {
            value = value * 31 + eventType.hashCode();
            value = value * 31 + unitId;
            value = value * 31 + address;
            value = value * 31 + quantity;
            hash = value;
        }
        return value;
    }

    public ModbusSlaveEventType getEventType() {
        return eventType;
    }

    public int getUnitId() {
        return unitId;
    }

    public int getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

}
