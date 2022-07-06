package osgi.common.util.excel.vo;

public class ExcelExportMapVO {

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
    private Class<?> javaType;

    /**
     * 表格列宽 N个字符*256
     */
    private int columnWidth;

    public ExcelExportMapVO(String property, String column, Class<?> javaType, int columnWidth) {
        super();
        this.property = property;
        this.column = column;
        this.javaType = javaType;
        this.columnWidth = columnWidth;
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

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /**
     * @return the columnWidth
     */
    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * @param columnWidth the columnWidth to set
     */
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

}
