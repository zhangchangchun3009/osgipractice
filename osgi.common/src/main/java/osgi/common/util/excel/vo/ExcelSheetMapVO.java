
package osgi.common.util.excel.vo;

import java.util.ArrayList;

/**
 * The Class ExcelSheetMapVO.
 *
 * @author zhangchangchun
 * @param <T> the generic type
 * @since 2022年1月4日
 */
public class ExcelSheetMapVO<T extends ExcelDataVO> {
    /**
     * excel数据页表单名
     */
    private String sheetName;

    private Class<T> voClass;

    private String consumerBeanName;

    /**
     * 表头与数据vo的映射配置
     */
    private ArrayList<ExcelRowMapVO<T>> importRowMapList;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Class<T> getVoClass() {
        return voClass;
    }

    public void setVoClass(Class<T> voClass) {
        this.voClass = voClass;
    }

    public ArrayList<ExcelRowMapVO<T>> getImportRowMapList() {
        return importRowMapList;
    }

    public void setImportRowMapList(ArrayList<ExcelRowMapVO<T>> importRowMapList) {
        this.importRowMapList = importRowMapList;
    }

    public String getConsumerBeanName() {
        return consumerBeanName;
    }

    public void setConsumerBeanName(String consumerBeanName) {
        this.consumerBeanName = consumerBeanName;
    }

}
