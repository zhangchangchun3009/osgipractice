
package osgi.common.vo;

/**
 * 异步任务结果数据
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public class AsyncTaskEventResultVO {

    private Long id;

    /** The task name. */
    private String taskName;

    /**
     * 创建：0；进行中：1；正常结束：2；异常终止9
     */
    private Integer status;

    /**
     * excel导入0；excel导出1；
     */
    private Integer eventType;

    private Long startTime;

    private Long endTime;

    private Integer createdBy;

    private String createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

}
