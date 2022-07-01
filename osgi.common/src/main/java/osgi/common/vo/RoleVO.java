
package osgi.common.vo;

import java.io.Serializable;
import java.util.List;

/**
 * The Class RoleVO.
 */
public class RoleVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 5317678657961912954L;

    private int id;

    private int roleId;

    private String roleName;

    private List<ResourceVO> privilege;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @return the privilege
     */
    public List<ResourceVO> getPrivilege() {
        return privilege;
    }

    /**
     * @param privilege the privilege to set
     */
    public void setPrivilege(List<ResourceVO> privilege) {
        this.privilege = privilege;
    }

}
