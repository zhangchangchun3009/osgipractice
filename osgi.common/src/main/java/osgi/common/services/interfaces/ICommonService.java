
package osgi.common.services.interfaces;

import osgi.common.vo.Response;

/**
 * 公共接口服务
 * 
 * @author zhangchangchun
 * @since 2021年5月13日
 */
public interface ICommonService {

    /**
     * Gets the sequence value.
     *
     * @return the sequence value by name
     */
    Response<Long> getSequenceValue();

    Response<Long> getSequenceValueByName(String sequenceName);

}
