package osgi.common.constants;

/**
 * 数据操作方式枚举
 * 
 * @author zhangchangchun
 * @since 2021年4月2日
 */
public enum DataHandlingModeEnum {

    INSERT("insert", "新增"), UPDATE("update", "修改"), DELETE("delete", "删除"), QUERY("select", "查询"), BATCHSAVE("batch",
            "批量保存");

    String code;

    String desc;

    DataHandlingModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

}
