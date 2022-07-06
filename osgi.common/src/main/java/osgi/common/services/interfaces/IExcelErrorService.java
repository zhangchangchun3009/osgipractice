
package osgi.common.services.interfaces;

import com.github.pagehelper.PageInfo;

import osgi.common.vo.BatchVO;
import osgi.common.vo.ExcelErrorVO;
import osgi.common.vo.PageVO;
import osgi.common.vo.Response;

/**
 * Excel错误处理服务
 *
 * @author zhangchangchun
 * @since 2022年1月4日
 */
public interface IExcelErrorService {

    PageInfo<ExcelErrorVO> getList(ExcelErrorVO param, PageVO pageVo);

    Response<ExcelErrorVO> batch(BatchVO<ExcelErrorVO> batchVO);
}
