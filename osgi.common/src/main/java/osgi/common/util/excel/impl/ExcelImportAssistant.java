
package osgi.common.util.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.common.Activator;
import osgi.common.constants.AsyncTaskStatusEnum;
import osgi.common.constants.AsyncTaskTypeEnum;
import osgi.common.constants.ExcelDataTypeEnums;
import osgi.common.services.interfaces.IAsyncTaskEventResultService;
import osgi.common.services.interfaces.IExcelErrorService;
import osgi.common.util.excel.interfaces.ExcelDataConsumerException;
import osgi.common.util.excel.interfaces.ExcelMapConfigException;
import osgi.common.util.excel.interfaces.ExcelProcessAbortedException;
import osgi.common.util.excel.interfaces.IExcelImportAssistant;
import osgi.common.util.excel.interfaces.IExcelImportDataConsumer;
import osgi.common.util.excel.vo.ExcelDataVO;
import osgi.common.util.excel.vo.ExcelImportVO;
import osgi.common.util.excel.vo.ExcelRowMapVO;
import osgi.common.util.excel.vo.ExcelSheetMapVO;
import osgi.common.vo.AsyncTaskEventResultVO;
import osgi.common.vo.BatchVO;
import osgi.common.vo.ExcelErrorVO;

/**
 * The Class ExcelImportAssistant.
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
@Named
public class ExcelImportAssistant implements IExcelImportAssistant {

    private static final int BATCH_SIZE = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelImportAssistant.class);

    @Inject
    private IAsyncTaskEventResultService asyncTaskService;

    @Inject
    private IExcelErrorService excelErrorService;

    @Inject
    @Named("threadPoolExecutor")
    private ThreadPoolExecutor executor;

    private Charset charSet = Charset.forName("gbk");

    @Override
    public void importExcel(InputStream fin, ExcelImportVO importContext) {
        Workbook workbook = getWorkbook(fin);
        if (workbook == null) {
            return;
        }

        Long eventId = createAsyncTask(importContext);

        executor.submit(() -> {
            ArrayList<ExcelSheetMapVO<?>> sheetMapperList = importContext.getImportSheetMapList();
            AtomicBoolean started = new AtomicBoolean(false);
            AtomicBoolean failed = new AtomicBoolean(false);
            @SuppressWarnings("unchecked")
            CompletableFuture<Void>[] futureArr = new CompletableFuture[sheetMapperList.size()];
            for (int i = 0; i < sheetMapperList.size(); i++) {
                ExcelSheetMapVO<?> sheetMapper = sheetMapperList.get(i);
                futureArr[i] = CompletableFuture.runAsync(() -> {
                    if (started.compareAndSet(false, true)) {
                        asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.RUNNING);
                    }
                    List<ExcelErrorVO> errorList = new LinkedList<>();
                    Sheet sheet = workbook.getSheet(sheetMapper.getSheetName());
                    ArrayList<? extends ExcelRowMapVO<?>> headerMapper = sheetMapper.getImportRowMapList();
                    checkHeader(eventId, errorList, sheet, headerMapper);
                    if (!errorList.isEmpty()) {
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                        return;
                    }
                    String consumerBeanName = sheetMapper.getConsumerBeanName();
                    IExcelImportDataConsumer consumer = (IExcelImportDataConsumer) Activator.bundleContextManager
                            .getBeanOfId(consumerBeanName);
                    ArrayList<ExcelDataVO> dataList = new ArrayList<>(1024);
                    try {
                        batchConsumeData(eventId, sheet, sheetMapper, errorList, dataList, importContext, consumer);
                    } catch (ExcelMapConfigException e) {
                        LOGGER.error("ExcelMapConfigException,", e);
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                        return;
                    } catch (ExcelProcessAbortedException e) {
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                        return;
                    } catch (ExcelDataConsumerException e) {
                        LOGGER.error("ExcelDataConsumerException,", e);
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                        return;
                    }
                    try {
                        consumeData(consumer, dataList, errorList, importContext);
                    } catch (ExcelProcessAbortedException e) {
                    } catch (ExcelDataConsumerException e) {
                        LOGGER.error("ExcelDataConsumerException,", e);
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                        return;
                    }
                    if (errorList.size() > 0) {
                        doBeforeExceptionallyReturn(eventId, failed, errorList);
                    }
                    return;
                }, executor);
            }
            try {
                CompletableFuture.allOf(futureArr).get();
                if (!failed.get()) {
                    asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.COMPLETED);
                }
            } catch (InterruptedException e) {
                LOGGER.error("excel import task interrupted");
                asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
                for (CompletableFuture<Void> future : futureArr) {
                    future.cancel(false);
                }
                return;
            } catch (ExecutionException e) {
                LOGGER.error("excel import task ExecutionException,", e);
                asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
                return;
            }
        });
    }

    private void doBeforeExceptionallyReturn(Long eventId, AtomicBoolean failed, List<ExcelErrorVO> errorList) {
        saveError(errorList);
        failed.compareAndSet(false, true);
        asyncTaskService.updateAsyncTaskStatus(eventId, AsyncTaskStatusEnum.STOPPED);
    }

    private void batchConsumeData(Long eventId, Sheet sheet, ExcelSheetMapVO<?> sheetMapper,
            List<ExcelErrorVO> errorList, ArrayList<ExcelDataVO> dataList, ExcelImportVO importContext,
            IExcelImportDataConsumer consumer)
            throws ExcelMapConfigException, ExcelProcessAbortedException, ExcelDataConsumerException {
        Class<? extends ExcelDataVO> voClass = sheetMapper.getVoClass();
        int total = sheet.getLastRowNum();
        ArrayList<? extends ExcelRowMapVO<?>> headerMapper = sheetMapper.getImportRowMapList();

        for (int i = 1; i < total; i++) {
            Row dataRow = sheet.getRow(i);
            ExcelDataVO vo = null;
            try {
                vo = voClass.newInstance();
                vo.setRowIdx(i + 1);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ExcelMapConfigException("excel import data vo doesn't have a constructor without arguments");
            }
            for (int j = 0; j < headerMapper.size(); j++) {
                ExcelRowMapVO<?> importMap = headerMapper.get(j);
                boolean required = importMap.getRequired();
                Cell cell = dataRow.getCell(j);
                CellType cellType = cell.getCellType();
                if (cellType == CellType.ERROR) {
                    errorList.add(new ExcelErrorVO(eventId, i + 1, j + 1, "单元格值包含错误"));
                    continue;
                }
                if (cellType == CellType.BLANK && required) {
                    errorList.add(new ExcelErrorVO(eventId, i + 1, j + 1, "单元格值不能为空"));
                    continue;
                }
                Object value = null;
                if (cellType != CellType.BLANK) {
                    boolean setSuccess = parseAndSetProp(value, cell, importMap, errorList, eventId, i, j);
                    if (!setSuccess) {
                        continue;
                    }
                }
                String prop = importMap.getProperty();
                try {
                    BeanUtils.setProperty(voClass, prop, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new ExcelMapConfigException("vo property " + prop + " setter invoke failed, check code");
                }
            }
            dataList.add(vo);
            if (dataList.size() >= BATCH_SIZE) {
                consumeData(consumer, dataList, errorList, importContext);
            }
        }
    }

    private void consumeData(IExcelImportDataConsumer consumer, ArrayList<ExcelDataVO> dataList,
            List<ExcelErrorVO> errorList, ExcelImportVO importContext)
            throws ExcelProcessAbortedException, ExcelDataConsumerException {
        boolean res = true;
        try {
            res = consumer.consumeData(dataList, errorList, importContext);
        } catch (Exception e) {
            LOGGER.error("data consumer consumeData err, import process is aborted,", e);
            throw new ExcelDataConsumerException();
        }
        if (!res) {
            throw new ExcelProcessAbortedException();
        }
        dataList.clear();
    }

    private void saveError(List<ExcelErrorVO> errorList) {
        int size = errorList.size();
        if (size == 0) {
            return;
        }
        int page = (size + BATCH_SIZE - 1) / BATCH_SIZE;
        for (int i = 0; i < page; i++) {
            List<ExcelErrorVO> subList = errorList.subList(i * BATCH_SIZE,
                    (i + 1) * BATCH_SIZE < size ? (i + 1) * BATCH_SIZE : size);
            BatchVO<ExcelErrorVO> batchVO = new BatchVO<>();
            List<ExcelErrorVO> itemsToCreate = new ArrayList<>();
            itemsToCreate.addAll(subList);
            batchVO.setItemsToCreate(itemsToCreate);
            excelErrorService.batch(batchVO);
        }
    }

    private Workbook getWorkbook(InputStream fin) {
        Workbook workbook = null;
        try {
            workbook = new HSSFWorkbook(fin);
        } catch (IOException e) {
            try {
                workbook = new XSSFWorkbook(fin);
            } catch (IOException e1) {
            }
        }
        return workbook;
    }

    private Long createAsyncTask(ExcelImportVO importContext) {
        Long eventId = asyncTaskService.gennerateTaskId();
        String fileName = importContext.getFileName();
        long now = System.currentTimeMillis();
        String taskName = fileName + "_" + now;
        AsyncTaskEventResultVO task = new AsyncTaskEventResultVO();
        task.setId(eventId);
        task.setTaskName(taskName);
        task.setStartTime(now);
        task.setStatus(AsyncTaskStatusEnum.CREATED.getValue());
        task.setEventType(AsyncTaskTypeEnum.IMPORT.getValue());
        task.setCreatedBy(importContext.getUser().getUserId());
        BatchVO<AsyncTaskEventResultVO> batchVO = new BatchVO<>();
        List<AsyncTaskEventResultVO> itemsToCreate = new ArrayList<>();
        itemsToCreate.add(task);
        batchVO.setItemsToCreate(itemsToCreate);
        asyncTaskService.batch(batchVO);
        return eventId;
    }

    private void checkHeader(Long eventId, List<ExcelErrorVO> errorList, Sheet sheet,
            ArrayList<? extends ExcelRowMapVO<?>> headerMapper) {
        Row row0 = sheet.getRow(0);
        for (int i = 0; i < headerMapper.size(); i++) {
            Cell cell = row0.getCell(i);
            String value = cell.getStringCellValue();
            String configColumn = headerMapper.get(i).getColumn();
            if (!value.equals(configColumn)) {
                errorList.add(new ExcelErrorVO(eventId, 1, i + 1, "列名与模板不符合，期望值是:" + configColumn));
            }
        }
    }

    private boolean parseAndSetProp(Object target, Cell source, ExcelRowMapVO<?> importMap,
            List<ExcelErrorVO> errorList, Long eventId, int rowIdx, int columnIdx) {
        Number min = importMap.getMinValue();
        Number max = importMap.getMaxValue();
        ExcelDataTypeEnums dataType = importMap.getJavaType();
        try {
            switch (dataType) {
            case String:
                String cellValueStr = source.getStringCellValue().trim();
                int length = cellValueStr.getBytes(charSet).length;
                if (cellValueStr != null && (length < min.intValue() || length > max.intValue())) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "字符串字节数超出可取值范围【" + min.intValue() + "," + max.intValue() + "】"));
                    return false;
                }
                target = cellValueStr;
                break;
            case Integer:
                Integer cellValueInt = Integer.valueOf((int) source.getNumericCellValue());
                if (cellValueInt < min.intValue() || cellValueInt > max.intValue()) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "整数值超出可取值范围【" + min.intValue() + "," + max.intValue() + "】"));
                    return false;
                }
                target = cellValueInt;
                break;
            case Long:
                Long cellValueLong = Long.valueOf((long) source.getNumericCellValue());
                if (cellValueLong < min.longValue() || cellValueLong > max.longValue()) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "长整数值超出可取值范围【" + min.longValue() + "," + max.longValue() + "】"));
                    return false;
                }
                target = cellValueLong;
                break;
            case BigInteger:
                BigInteger cellValueBigInt = new BigInteger(source.getStringCellValue().trim());
                if (cellValueBigInt.compareTo((BigInteger) min) == -1
                        || cellValueBigInt.compareTo((BigInteger) max) == 1) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "大整数值超出可取值范围【" + min.toString() + "," + max.toString() + "】"));
                    return false;
                }
                target = cellValueBigInt;
                break;
            case BigDecimal:
                BigDecimal cellValueBigDecimal = new BigDecimal(source.getStringCellValue().trim());
                if (cellValueBigDecimal.compareTo((BigDecimal) min) == -1
                        || cellValueBigDecimal.compareTo((BigDecimal) max) == 1) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "大小数值超出可取值范围【" + min.toString() + "," + max.toString() + "】"));
                    return false;
                }
                target = cellValueBigDecimal;
                break;
            case Float:
                Float cellValueFloat = Float.valueOf((float) source.getNumericCellValue());
                if (cellValueFloat.compareTo((Float) min) == -1 || cellValueFloat.compareTo((Float) max) == 1) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "小数值超出可取值范围【" + min.toString() + "," + max.toString() + "】"));
                    return false;
                }
                target = cellValueFloat;
                break;
            case Double:
                Double cellValueDouble = Double.valueOf(source.getNumericCellValue());
                if (cellValueDouble.compareTo((Double) min) == -1 || cellValueDouble.compareTo((Double) max) == 1) {
                    errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                            "小数值超出可取值范围【" + min.toString() + "," + max.toString() + "】"));
                    return false;
                }
                target = cellValueDouble;
                break;
            default:
                errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1, "单元格值属于不支持的字符串和数字类型"));
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("cell value parse err,", e);
            errorList.add(new ExcelErrorVO(eventId, rowIdx + 1, columnIdx + 1,
                    "单元格数值转换异常，请严格按模板样例检查输入数据类型，注意单元格格式，有些较大的数可能需要按文本格式输入"));
            return false;
        }
        return true;
    }

}
