package osgi.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import osgi.common.vo.ResourceVO;
import osgi.common.vo.RoleVO;
import osgi.common.vo.UserVO;

/**
 * The Interface IPrivilegeDao.
 */
public interface IPrivilegeDao {

    /**
     * Gets the role list.
     *
     * @param role the role
     * @return the role list
     */
    List<RoleVO> getRoleList(@Param("entity") RoleVO role);

    /**
     * Batch insert.
     *
     * @param resourceList the resource list
     */
    void batchInsert(@Param("list") List<ResourceVO> resourceList);

    /**
     * Delete all.
     */
    void deleteAllInvalid();

    /**
     * Gets the role list by user.
     *
     * @param user the user
     * @return the role list by user
     */
    List<RoleVO> getRoleListByUser(@Param("entity") UserVO user);

    /**
     * Insert role by user.
     *
     * @param roles the roles
     * @param userId the user id
     */
    void insertRoleByUser(@Param("list") List<RoleVO> roles, @Param("userId") Integer userId);

    /**
     * Delete role by user.
     *
     * @param roles the roles
     * @param userId the user id
     */
    void deleteRoleByUser(@Param("list") List<RoleVO> roles, @Param("userId") Integer userId);

    /**
     * Insert role.
     *
     * @param roleList the role list
     */
    void insertRole(@Param("list") List<RoleVO> roleList);

    /**
     * Delete role.
     *
     * @param roleList the role list
     */
    void deleteRole(@Param("list") List<RoleVO> roleList);

    /**
     * Gets the resource list by role.
     *
     * @param role the role
     * @return the resource list by role
     */
    List<ResourceVO> getResourceListByRole(@Param("entity") RoleVO role);

    /**
     * Insert resource by role.
     *
     * @param resourceList the resource list
     * @param roleId the role id
     */
    void insertResourceByRole(@Param("list") List<ResourceVO> resourceList, @Param("roleId") Integer roleId);

    /**
     * Delete resource by role.
     *
     * @param resourceList the resource list
     * @param roleId the role id
     */
    void deleteResourceByRole(@Param("list") List<ResourceVO> resourceList, @Param("roleId") Integer roleId);

    /**
     * Query resource list.
     *
     * @return the list
     */
    List<ResourceVO> queryResourceList();

    /**
     * Delete all resource by role.
     *
     * @param roleId the role id
     */
    void deleteAllResourceByRole(@Param("roleId") Integer roleId);

}
