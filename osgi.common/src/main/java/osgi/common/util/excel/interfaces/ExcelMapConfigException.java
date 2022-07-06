
package osgi.common.util.excel.interfaces;

/**
 * 导入接口程序编写有异常，应当检查导入代码消除错误
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public class ExcelMapConfigException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    public ExcelMapConfigException(String string) {
        super(string);
    }
}
