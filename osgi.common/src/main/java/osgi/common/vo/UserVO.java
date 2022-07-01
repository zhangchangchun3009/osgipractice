
package osgi.common.vo;

import java.io.Serializable;
import java.util.List;

/**
 * The Class UserVO.
 */
public class UserVO extends BaseVO implements Serializable {
    private static final long serialVersionUID = -8324281584406379838L;

    private int id;

    private int userId;

    private String userName;

    private String password;

    /**
     * 用户类型：system，default，virtual
     */
    private String userType;

    private List<RoleVO> roles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the roles
     */
    public List<RoleVO> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<RoleVO> roles) {
        this.roles = roles;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the userType
     */
    public String getUserType() {
        return userType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

}
