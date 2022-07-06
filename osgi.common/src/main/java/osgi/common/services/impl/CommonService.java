
package osgi.common.services.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.common.dao.ISequenceDao;
import osgi.common.services.interfaces.ICommonService;
import osgi.common.vo.Response;

/**
 * 公共服务
 * 
 * @author zhangchangchun
 * @since 2021年5月13日
 */
@Named
public class CommonService implements ICommonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);

    /** The sequence dao. */
    @Inject
    private ISequenceDao sequenceDao;

    @Override
    public Response<Long> getSequenceValue() {
        Response<Long> response = new Response<Long>();
        long data = 0L;
        try {
            data = sequenceDao.getNextValue("s_common");
        } catch (Exception e) {
            LOGGER.error("getSequenceValue Exception,", e);
            return response.fail("001", e.getMessage());
        }
        return response.success(data);
    }

    @Override
    public Response<Long> getSequenceValueByName(String sequenceName) {
        Response<Long> response = new Response<Long>();
        if (sequenceName == null || sequenceName.equals("")) {
            return response.fail("001", "序列名不能为空");
        }
        long data = 0L;
        try {
            data = sequenceDao.getNextValue(sequenceName);
        } catch (Exception e) {
            LOGGER.error("getSequenceValueByName Exception,", e);
            return response.fail("002", "序列名不存在");
        }
        return response.success(data);
    }

}
