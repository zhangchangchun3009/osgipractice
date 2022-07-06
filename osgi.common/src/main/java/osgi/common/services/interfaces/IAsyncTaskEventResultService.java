
package osgi.common.services.interfaces;

import com.github.pagehelper.PageInfo;

import osgi.common.constants.AsyncTaskStatusEnum;
import osgi.common.vo.AsyncTaskEventResultVO;
import osgi.common.vo.BatchVO;
import osgi.common.vo.PageVO;
import osgi.common.vo.Response;

/**
 * 异步任务结果服务
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public interface IAsyncTaskEventResultService {

    PageInfo<AsyncTaskEventResultVO> getList(AsyncTaskEventResultVO param, PageVO pageVo);

    Response<AsyncTaskEventResultVO> batch(BatchVO<AsyncTaskEventResultVO> batchVO);

    void updateAsyncTaskStatus(Long eventId, AsyncTaskStatusEnum status);

    Long gennerateTaskId();

}
