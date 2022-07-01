package osgi.common.constants;

/**
 * 异步任务结果状态枚举
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public enum AsyncTaskStatusEnum {

    CREATED(0), RUNNING(1), COMPLETED(2), STOPPED(9);

    private int value;

    AsyncTaskStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
