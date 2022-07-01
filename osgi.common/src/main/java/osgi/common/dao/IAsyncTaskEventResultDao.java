package osgi.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import osgi.common.vo.AsyncTaskEventResultVO;

public interface IAsyncTaskEventResultDao {

    List<AsyncTaskEventResultVO> getList(@Param("entity") AsyncTaskEventResultVO param);

    void insert(@Param("list") List<AsyncTaskEventResultVO> itemsToCreate);

    void update(@Param("list") List<AsyncTaskEventResultVO> itemsToUpdate);

    void delete(@Param("list") List<AsyncTaskEventResultVO> itemsToDelete);

}
