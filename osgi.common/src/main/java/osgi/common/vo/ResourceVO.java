
package osgi.common.vo;

import java.io.Serializable;

/**
 * The Class ResourceVO.
 *
 * @author zhangchangchun
 * @since 2021年4月12日
 */
public class ResourceVO implements Serializable {
    private static final long serialVersionUID = -7374766355735807017L;

    private int id;

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 服务编码
     */
    private String serviceCode;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 资源方法编码
     */
    private String methodCode;

    /**
     * 资源方法名
     */
    private String methodName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

}
