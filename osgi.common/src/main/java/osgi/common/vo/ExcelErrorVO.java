
package osgi.common.vo;

/**
 * excel错误
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public class ExcelErrorVO {

    private Long id;

    private Long eventId;

    private Integer rowIndex;

    private Integer columnIndex;

    private String message;

    public ExcelErrorVO(Long eventId, Integer rowIndex, Integer columnIndex, String message) {
        super();
        this.eventId = eventId;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
