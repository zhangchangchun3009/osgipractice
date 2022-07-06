package osgi.common.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import osgi.common.constants.AsyncTaskStatusEnum;
import osgi.common.dao.IAsyncTaskEventResultDao;
import osgi.common.services.interfaces.IAsyncTaskEventResultService;
import osgi.common.services.interfaces.ICommonService;
import osgi.common.vo.AsyncTaskEventResultVO;
import osgi.common.vo.BatchVO;
import osgi.common.vo.PageVO;
import osgi.common.vo.Response;

@Named
public class AsyncTaskEventResultService implements IAsyncTaskEventResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskEventResultService.class);

    @Inject
    private ICommonService commonService;

    @Inject
    private IAsyncTaskEventResultDao asyncTaskEventResultDao;

    @Override
    public PageInfo<AsyncTaskEventResultVO> getList(AsyncTaskEventResultVO param, PageVO pageVo) {
        PageHelper.startPage(pageVo.getPageNum(), pageVo.getPageSize());
        List<AsyncTaskEventResultVO> list = asyncTaskEventResultDao.getList(param);
        PageInfo<AsyncTaskEventResultVO> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public Response<AsyncTaskEventResultVO> batch(BatchVO<AsyncTaskEventResultVO> batchVO) {
        Response<AsyncTaskEventResultVO> response = new Response<AsyncTaskEventResultVO>().success();
        if (batchVO == null) {
            return response;
        }

        List<AsyncTaskEventResultVO> itemsToCreate = batchVO.getItemsToCreate();
        List<AsyncTaskEventResultVO> itemsToUpdate = batchVO.getItemsToUpdate();
        List<AsyncTaskEventResultVO> itemsToDelete = batchVO.getItemsToDelete();
        if (!CollectionUtils.isEmpty(itemsToCreate)) {
            try {
                asyncTaskEventResultDao.insert(itemsToCreate);
            } catch (Exception e) {
                LOGGER.error("batch insert exception:", e);
                return response.fail("001", "数据库插入数据出错");
            }
        }
        if (!CollectionUtils.isEmpty(itemsToUpdate)) {
            try {
                asyncTaskEventResultDao.update(itemsToUpdate);
            } catch (Exception e) {
                LOGGER.error("batch update exception:", e);
                return response.fail("002", "数据库修改数据出错");
            }
        }
        if (!CollectionUtils.isEmpty(itemsToDelete)) {
            try {
                asyncTaskEventResultDao.delete(itemsToDelete);
            } catch (Exception e) {
                LOGGER.error("batch delete exception:", e);
                return response.fail("003", "数据库删除数据出错");
            }
        }
        return response;

    }

    @Override
    public void updateAsyncTaskStatus(Long eventId, AsyncTaskStatusEnum status) {
        BatchVO<AsyncTaskEventResultVO> batchVO = new BatchVO<>();
        List<AsyncTaskEventResultVO> itemsToUpdate = new ArrayList<>();
        AsyncTaskEventResultVO task = new AsyncTaskEventResultVO();
        task.setId(eventId);
        task.setStatus(status.getValue());
        if (status == AsyncTaskStatusEnum.COMPLETED || status == AsyncTaskStatusEnum.STOPPED) {
            task.setEndTime(System.currentTimeMillis());
        }
        itemsToUpdate.add(task);
        batchVO.setItemsToUpdate(itemsToUpdate);
        batch(batchVO);
    }

    @Override
    public Long gennerateTaskId() {
        return commonService.getSequenceValueByName("s_asynctaskid").getData();
    }

}
