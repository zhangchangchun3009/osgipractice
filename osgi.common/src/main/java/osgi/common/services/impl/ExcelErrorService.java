package osgi.common.services.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import osgi.common.dao.IExcelErrorDao;
import osgi.common.services.interfaces.IExcelErrorService;
import osgi.common.vo.BatchVO;
import osgi.common.vo.ExcelErrorVO;
import osgi.common.vo.PageVO;
import osgi.common.vo.Response;

@Named
public class ExcelErrorService implements IExcelErrorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelErrorService.class);

    @Inject
    private IExcelErrorDao excelErrorDao;

    @Override
    public PageInfo<ExcelErrorVO> getList(ExcelErrorVO param, PageVO pageVo) {
        PageHelper.startPage(pageVo.getPageNum(), pageVo.getPageSize());
        List<ExcelErrorVO> list = excelErrorDao.getList(param);
        PageInfo<ExcelErrorVO> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public Response<ExcelErrorVO> batch(BatchVO<ExcelErrorVO> batchVO) {
        Response<ExcelErrorVO> response = new Response<ExcelErrorVO>().success();
        if (batchVO == null) {
            return response;
        }

        List<ExcelErrorVO> itemsToCreate = batchVO.getItemsToCreate();
        List<ExcelErrorVO> itemsToUpdate = batchVO.getItemsToUpdate();
        List<ExcelErrorVO> itemsToDelete = batchVO.getItemsToDelete();
        if (!CollectionUtils.isEmpty(itemsToCreate)) {
            try {
                excelErrorDao.insert(itemsToCreate);
            } catch (Exception e) {
                LOGGER.error("batch insert exception:", e);
                return response.fail("001", "数据库插入数据出错");
            }
        }
        if (!CollectionUtils.isEmpty(itemsToUpdate)) {
            try {
                excelErrorDao.update(itemsToUpdate);
            } catch (Exception e) {
                LOGGER.error("batch update exception:", e);
                return response.fail("002", "数据库修改数据出错");
            }
        }
        if (!CollectionUtils.isEmpty(itemsToDelete)) {
            try {
                excelErrorDao.delete(itemsToDelete);
            } catch (Exception e) {
                LOGGER.error("batch delete exception:", e);
                return response.fail("003", "数据库删除数据出错");
            }
        }
        return response;
    }

}
