
package osgi.common.util.excel.vo;

import java.util.ArrayList;
import java.util.List;

import osgi.common.vo.UserVO;

/**
 * excel导出配置
 * 
 * @author zhangchangchun
 * @since 2021年4月8日
 */
public class ExcelExportVO {
    /**
     * 导出文件名
     */
    private String fileName;

    /**
     * excel数据页表单名
     */
    private String sheetName;

    /**
     * 查询数据dao
     */
    private Object queryDao;

    /**
     * 查询方法
     */
    private String queryMethodName;

    /**
     * 查询参数
     */
    private ArrayList<Object> queryParamList;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 表头与数据vo的映射配置
     */
    private List<ExcelExportMapVO> exportMapList;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Object getQueryDao() {
        return queryDao;
    }

    public void setQueryDao(Object queryDao) {
        this.queryDao = queryDao;
    }

    public String getQueryMethodName() {
        return queryMethodName;
    }

    public void setQueryMethodName(String queryMethodName) {
        this.queryMethodName = queryMethodName;
    }

    /**
     * @return the exportMapList
     */
    public List<ExcelExportMapVO> getExportMapList() {
        return exportMapList;
    }

    /**
     * @param exportMapList the exportMapList to set
     */
    public void setExportMapList(List<ExcelExportMapVO> exportMapList) {
        this.exportMapList = exportMapList;
    }

    /**
     * @return the user
     */
    public UserVO getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserVO user) {
        this.user = user;
    }

    /**
     * @return the queryParamList
     */
    public ArrayList<Object> getQueryParamList() {
        return queryParamList;
    }

    /**
     * @param queryParamList the queryParamList to set
     */
    public void setQueryParamList(ArrayList<Object> queryParamList) {
        this.queryParamList = queryParamList;
    }

}
