
package osgi.common.util.excel.vo;

import java.util.ArrayList;
import java.util.List;

import osgi.common.vo.UserVO;

/**
 * The Class ExcelImportVO.
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public class ExcelImportVO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 表单配置
     */
    private ArrayList<ExcelSheetMapVO<?>> importSheetMapList;

    /**
     * 传递自定义上下文，可用在数据消费类获取
     */
    private List<Object> definedContext;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public ArrayList<ExcelSheetMapVO<?>> getImportSheetMapList() {
        return importSheetMapList;
    }

    public void setImportSheetMapList(ArrayList<ExcelSheetMapVO<?>> importSheetMapList) {
        this.importSheetMapList = importSheetMapList;
    }

    public List<Object> getDefinedContext() {
        return definedContext;
    }

    public void setDefinedContext(List<Object> definedContext) {
        this.definedContext = definedContext;
    }
}
