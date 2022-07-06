
package osgi.common.util.excel.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import osgi.common.constants.AsyncTaskStatusEnum;
import osgi.common.constants.AsyncTaskTypeEnum;
import osgi.common.services.interfaces.IAsyncTaskEventResultService;
import osgi.common.util.excel.interfaces.ExcelMapConfigException;
import osgi.common.util.excel.interfaces.IExcelExportAssistant;
import osgi.common.util.excel.interfaces.IExcelExportDataProvider;
import osgi.common.util.excel.vo.ExcelExportMapVO;
import osgi.common.util.excel.vo.ExcelExportVO;
import osgi.common.vo.AsyncTaskEventResultVO;
import osgi.common.vo.BatchVO;

/**
 * The Class ExcelExportAssistant.
 *
 * @author zhangchangchun
 * @since 2021年4月8日
 */
@Named
public class ExcelExportAssistant implements IExcelExportAssistant {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportAssistant.class);

    /**
     * 分批导出数据数量
     */
    private static final int PAGESIZE = 500;

    @Inject
    @Named("threadPoolExecutor")
    private ThreadPoolExecutor executor;

    @Inject
    private IAsyncTaskEventResultService asyncTaskService;

    /**
     * 根据查询条件从数据库获取数据并导出为excel文件
     * 文件输出方式暂定为同步流式响应，后续可改造为异步处理
     * 文件写到磁盘
     * @param request http请求
     * @param response the http响应
     * @param excelExportVO 导出配置，包括对象映射关系和数据库数据源查询配置
     * @throws Exception 各种异常
     */
    @Override
    public String export(final ExcelExportVO excelExportVO) throws Exception {
//        response.setContentType("application/vnd.ms-excel");
        String fileName = excelExportVO.getFileName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + ".xls";
//        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));

        long eventId = createAsyncTask(excelExportVO);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.RUNNING);
                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFCellStyle headStyle = setHeaderStyle(workbook);
                HSSFCellStyle dataStyle = setDataStyle(workbook);

                HSSFSheet sheet = workbook.createSheet(excelExportVO.getSheetName());

                List<ExcelExportMapVO> headerMap = excelExportVO.getExportMapList();

                setHeaderRow(headStyle, sheet, headerMap);

                PageHelper.startPage(1, PAGESIZE);
                try {
                    Page<?> dataList1 = getData(excelExportVO);
                    if (dataList1.getTotal() < 1) {
                        asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.COMPLETED);
                        return;
                    }
                    setBatchDataRow(dataStyle, sheet, headerMap, dataList1, 1);

                    int pages = dataList1.getPages();
                    for (int i = 2; i <= pages; i++) {
                        PageHelper.startPage(i, PAGESIZE);
                        Page<?> dataListn = getData(excelExportVO);
                        setBatchDataRow(dataStyle, sheet, headerMap, dataListn, i);
                    }
                } catch (ExcelMapConfigException e) {
                    LOGGER.error("ExcelMapConfigException", e);
                    asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
                    return;
                }

                try {
//                  workbook.write(response.getOutputStream());
                    String dir = (SystemUtils.IS_OS_WINDOWS ? "D:" : "/usr/zcc") + File.separator + "excel"
                            + File.separator + "export";
                    File direc = new File(dir);
                    if (!direc.exists()) {
                        direc.mkdirs();
                    }
                    String filePath = dir + File.separator + fileName;
                    workbook.write(new File(filePath));
                    workbook.close();
                    asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.COMPLETED);
                } catch (IOException e) {
                    asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
                    return;
                }
                return;
            }
        };
        CompletableFuture.runAsync(task, executor).whenComplete((res, ex) -> {
            if (ex != null) {
                asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
            }
        });
        return fileName;
    }

    private Long createAsyncTask(ExcelExportVO exportContext) {
        Long eventId = asyncTaskService.gennerateTaskId();
        String fileName = exportContext.getFileName();
        long now = System.currentTimeMillis();
        String taskName = fileName + "_" + now;
        AsyncTaskEventResultVO task = new AsyncTaskEventResultVO();
        task.setId(eventId);
        task.setTaskName(taskName);
        task.setStartTime(now);
        task.setStatus(AsyncTaskStatusEnum.CREATED.getValue());
        task.setEventType(AsyncTaskTypeEnum.EXPORT.getValue());
        task.setCreatedBy(exportContext.getUser().getUserId());
        BatchVO<AsyncTaskEventResultVO> batchVO = new BatchVO<>();
        List<AsyncTaskEventResultVO> itemsToCreate = new ArrayList<>();
        itemsToCreate.add(task);
        batchVO.setItemsToCreate(itemsToCreate);
        asyncTaskService.batch(batchVO);
        return eventId;
    }

    private static void setHeaderRow(HSSFCellStyle headStyle, HSSFSheet sheet, List<ExcelExportMapVO> headerMap) {
        HSSFRow row0 = sheet.createRow(0);
        for (int i = 0; i < headerMap.size(); i++) {
            ExcelExportMapVO map = headerMap.get(i);
            sheet.setColumnWidth(i, map.getColumnWidth());
            HSSFCell cell = row0.createCell(i);
            cell.setCellValue(map.getColumn());
            cell.setCellStyle(headStyle);
        }
    }

    private static void setBatchDataRow(HSSFCellStyle dataStyle, HSSFSheet sheet, List<ExcelExportMapVO> headerMap,
            Page<?> dataList, int pageNum) throws ExcelMapConfigException {
        for (int i = 0; i < dataList.size(); i++) {
            HSSFRow row = sheet.createRow((pageNum - 1) * PAGESIZE + i + 1);
            Object data = dataList.get(i);
            for (int j = 0; j < headerMap.size(); j++) {
                ExcelExportMapVO map = headerMap.get(j);
                HSSFCell cell = row.createCell(j);
                String propertyValue = "";
                try {
                    propertyValue = BeanUtils.getProperty(data, map.getProperty());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new ExcelMapConfigException("excel 导出配置有误");
                }
                cell.setCellValue(propertyValue);
                cell.setCellStyle(dataStyle);
            }
        }
    }

    private static Page<?> getData(ExcelExportVO excelExportVO) throws ExcelMapConfigException {
        Object dao = excelExportVO.getQueryDao();
        String queryMethodName = excelExportVO.getQueryMethodName();
        List<Object> queryParamList = excelExportVO.getQueryParamList();
        Class<?>[] parameterTypes = null;
        Object[] parameterValues = null;
        if (queryParamList != null && !queryParamList.isEmpty()) {
            parameterTypes = new Class<?>[queryParamList.size()];
            parameterValues = new Object[queryParamList.size()];
            for (int i = 0; i < queryParamList.size(); i++) {
                parameterTypes[i] = queryParamList.get(i).getClass();
                parameterValues[i] = queryParamList.get(i);
            }
        }
        Object dataS = null;
        try {
            Method method = dao.getClass().getDeclaredMethod(queryMethodName, parameterTypes);
            dataS = method.invoke(dao, parameterValues);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ExcelMapConfigException("excel导出数据库查询相关配置有问题");
        }
        if (dataS == null) {
            return new Page<Object>();
        }
        Page<?> dataList = (Page<?>) dataS;
        return dataList;
    }

    private static HSSFCellStyle setHeaderStyle(HSSFWorkbook workbook) {
        HSSFCellStyle headStyle = workbook.createCellStyle(); // 表头
        HSSFFont headFont = workbook.createFont(); // 表头字体
        headFont.setFontName("黑体");
        headFont.setBold(true); // 粗体
        headFont.setFontHeightInPoints((short) 14); // 字号
        headStyle.setFont(headFont);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        headStyle.setWrapText(true); // 自动换行
        headStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        headStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headStyle;
    }

    private static HSSFCellStyle setDataStyle(HSSFWorkbook workbook) {
        HSSFCellStyle dataStyle = workbook.createCellStyle(); // 表头
        HSSFFont dataFont = workbook.createFont(); // 表头字体
        dataFont.setFontName("黑体");
        dataFont.setBold(false); // 粗体
        dataFont.setFontHeightInPoints((short) 10); // 字号
        dataStyle.setFont(dataFont);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.LEFT); // 水平居左
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        dataStyle.setWrapText(true); // 自动换行
        dataStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
        dataStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
        dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return dataStyle;
    }

    /**
     * 自定义数据源导出为excel
     * 导出文件输出到D:\excel\export目录
     * 只支持同步处理。由于是内存数据源所以性能应该比较快，问题是内存数据占用内存，所以不适合大量数据
     * 成功时无返回，错误时抛出异常
     *
     * @param <T> the generic type 数据源的数据java类型
     * @param provider the provider 数据提供者，返回有序集合，不支持分批数据源
     * @param excelExportVO 导出配置，不需要配置数据库数据源查询相关
     * @return 导出成功时返回文件名，依文件名调下载接口下载文件
     * @throws Exception the exception 各种异常
     */
    @Override
    public <T> String export(IExcelExportDataProvider<T> provider, final ExcelExportVO excelExportVO) throws Exception {
        String fileName = excelExportVO.getFileName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + ".xls";
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle headStyle = setHeaderStyle(workbook);
        HSSFCellStyle dataStyle = setDataStyle(workbook);

        HSSFSheet sheet = workbook.createSheet(excelExportVO.getSheetName());

        List<ExcelExportMapVO> headerMap = excelExportVO.getExportMapList();

        ArrayList<T> dataList = provider.getData();
        if (dataList == null || dataList.size() < 1) {
            return "";
        }

        setHeaderRow(headStyle, sheet, headerMap);

        setBatchDataRow(dataStyle, sheet, headerMap, dataList);
        try {
            String dir = (SystemUtils.IS_OS_WINDOWS ? "D:" : "/usr/zcc") + File.separator + "excel" + File.separator
                    + "export";
            File direc = new File(dir);
            if (!direc.exists()) {
                direc.mkdirs();
            }
            String filePath = dir + File.separator + fileName;
            workbook.write(new File(filePath));
            workbook.close();
            return fileName;
        } catch (IOException e) {
            throw e;
        }
    }

    private static <T> void setBatchDataRow(HSSFCellStyle dataStyle, HSSFSheet sheet, List<ExcelExportMapVO> headerMap,
            ArrayList<T> dataList) throws ExcelMapConfigException {
        for (int i = 0; i < dataList.size(); i++) {
            HSSFRow row = sheet.createRow(i + 1);
            Object data = dataList.get(i);
            for (int j = 0; j < headerMap.size(); j++) {
                ExcelExportMapVO map = headerMap.get(j);
                HSSFCell cell = row.createCell(j);
                String propertyValue = "";
                try {
                    propertyValue = BeanUtils.getProperty(data, map.getProperty());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new ExcelMapConfigException("excel 导出配置有误");
                }
                cell.setCellValue(propertyValue);
                cell.setCellStyle(dataStyle);
            }
        }
    }

}
