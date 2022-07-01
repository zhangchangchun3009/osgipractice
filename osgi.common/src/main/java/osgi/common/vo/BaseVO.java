
package osgi.common.vo;

import java.io.Serializable;

/**
 * VO 基类
 * 
 * @author zhangchangchun
 * @since 2021年4月1日
 */
public class BaseVO implements Serializable {

    private static final long serialVersionUID = 4348297656858764851L;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private String createdTime;

    /**
     * 最后修改人
     */
    private String lastUpdatedBy;

    /**
     * 最后修改时间
     */
    private String lastUpdatedTime;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

}
