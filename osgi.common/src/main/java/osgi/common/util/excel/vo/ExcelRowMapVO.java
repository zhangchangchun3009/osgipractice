
package osgi.common.util.excel.vo;

import osgi.common.constants.ExcelDataTypeEnums;

/**
 * The Class ExcelRowMapVO.
 *
 * @author zhangchangchun
 * @param <T> the generic type
 * @since 2022年1月4日
 */
public class ExcelRowMapVO<T> {
    /**
     * VO属性名
     */
    private String property;

    /**
     * 表格列名
     */
    private String column;

    /**
     * 数据的Java类型
     */
    private ExcelDataTypeEnums javaType;

    private boolean required;

    private Number minValue;

    private Number maxValue;

    /**
     * @param property VO属性名
     * @param column 表格列名
     * @param javaType 数据的Java类型enum
     * @param required 是否必填
     * @param minValue 最小值，字符串是字节数，数值类型的需传递与vo属性类型一致的值
     * @param maxValue 最大值，字符串是字节数，数值类型的需传递与vo属性类型一致的值
     */
    public ExcelRowMapVO(String property, String column, ExcelDataTypeEnums javaType, boolean required, Number minValue,
            Number maxValue) {
        super();
        this.property = property;
        this.column = column;
        this.javaType = javaType;
        this.required = required;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public ExcelDataTypeEnums getJavaType() {
        return javaType;
    }

    public void setJavaType(ExcelDataTypeEnums javaType) {
        this.javaType = javaType;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Number getMinValue() {
        return minValue;
    }

    public void setMinValue(Number minValue) {
        this.minValue = minValue;
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Number maxValue) {
        this.maxValue = maxValue;
    }

}
