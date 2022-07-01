package osgi.common.websocket.service.interfaces;

public enum SocketCodeEnum {

    CONMUNICATION_CODE("010"), LISTEN_STOP_CODE("002"), LISTEN_START_CODE("001");

    String code;

    SocketCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
