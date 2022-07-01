package osgi.common.util.modbustcp.slave.db;

public class DatabaseNotInServiceException extends RuntimeException {
    private static final long serialVersionUID = 4051508341855858392L;

    public DatabaseNotInServiceException() {
    }

    public DatabaseNotInServiceException(String msg) {
        super(msg);
    }

}
