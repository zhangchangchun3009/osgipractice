package osgi.common.util.excel.interfaces;

import java.io.InputStream;

import osgi.common.util.excel.vo.ExcelImportVO;

public interface IExcelImportAssistant {

    void importExcel(InputStream fin, ExcelImportVO importContext);

}
