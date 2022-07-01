package osgi.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import osgi.common.vo.ExcelErrorVO;

public interface IExcelErrorDao {

    List<ExcelErrorVO> getList(@Param("entity") ExcelErrorVO param);

    void insert(@Param("list") List<ExcelErrorVO> itemsToCreate);

    void update(@Param("list") List<ExcelErrorVO> itemsToUpdate);

    void delete(@Param("list") List<ExcelErrorVO> itemsToDelete);

}
