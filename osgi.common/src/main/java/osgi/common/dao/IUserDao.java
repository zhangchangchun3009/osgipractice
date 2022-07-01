package osgi.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import osgi.common.vo.ResourceVO;
import osgi.common.vo.UserVO;

public interface IUserDao {

    UserVO findUser(@Param("entity") UserVO queryParam);

    int hasPrivilege(@Param("user") UserVO user, @Param("resource") ResourceVO param);

    int generateUserId();

    void insert(@Param("user") UserVO user);

    List<UserVO> getPagedUserList(@Param("entity") UserVO param);

    void delete(@Param("entity") UserVO param);

}
