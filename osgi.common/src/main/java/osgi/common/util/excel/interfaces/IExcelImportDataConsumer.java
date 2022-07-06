package osgi.common.util.excel.interfaces;

import java.util.ArrayList;
import java.util.List;

import osgi.common.util.excel.vo.ExcelDataVO;
import osgi.common.util.excel.vo.ExcelImportVO;
import osgi.common.vo.ExcelErrorVO;

public interface IExcelImportDataConsumer {

    /**
     * 消费转换好的excel数据，实例bean名称需要在导入配置类里设置
     * @param dataList excel数据，分批次传入消费，默认500条一批
     * @param errorList 导入错误，会累计，不会被清除
     * @param importContext 导入上下文，可在导入接口设置需要传递的属性
     * @return 返回false时中断导入过程
     */
    boolean consumeData(ArrayList<ExcelDataVO> dataList, List<ExcelErrorVO> errorList, ExcelImportVO importContext);

}
