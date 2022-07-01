package osgi.common.constants;

/**
 * excel事件类型枚举
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public enum AsyncTaskTypeEnum {
    IMPORT(0), EXPORT(1);

    private int value;

    AsyncTaskTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
