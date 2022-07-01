package osgi.common.util.modbustcp.slave.db;

public class SerialData {

    private String unitId;

    private short[] data;

    public SerialData() {
    }

    public SerialData(String unitId, short[] data) {
        this.unitId = unitId;
        this.data = data;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public short[] getData() {
        return data;
    }

    public void setData(short[] data) {
        this.data = data;
    }

}
