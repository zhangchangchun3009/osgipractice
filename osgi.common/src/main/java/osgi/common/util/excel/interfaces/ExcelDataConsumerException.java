
package osgi.common.util.excel.interfaces;

/**
 * Excel数据消费程序出了异常，导入程序将终止。推荐消费类捕获和处理所有异常，不要抛出异常
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public class ExcelDataConsumerException extends Exception {

    private static final long serialVersionUID = 1L;

}
