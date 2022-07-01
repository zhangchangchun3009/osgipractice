package osgi.common.constants;

/**
 * The Enum UserTypeEnum.
 */
/**
 * @author 张常春
 * @since 2021年6月15日
 */
public enum UserTypeEnum {
    SYSTEM("system"), VIRTUAL("virtual"), DEFAULT("default");

    private String value;

    private UserTypeEnum(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
