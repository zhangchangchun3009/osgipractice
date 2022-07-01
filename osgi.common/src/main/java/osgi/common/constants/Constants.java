package osgi.common.constants;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * 常量类
 * 
 * @author zhangchangchun
 * @since 2021年4月1日
 */
public interface Constants {

    /**
     * 批处理权限编码
     */
    String PRIVILEGE_INSERT_CODE = "insert";

    /**
     * 批处理权限描述
     */
    String PRIVILEGE_INSERT_DESC = "新增";

    /**
     * 批处理权限编码
     */
    String PRIVILEGE_UPDATE_CODE = "update";

    /**
     * 批处理权限描述
     */
    String PRIVILEGE_UPDATE_DESC = "修改";

    /**
     * 批处理权限编码
     */
    String PRIVILEGE_DELETE_CODE = "delete";

    /**
     * 批处理权限描述
     */
    String PRIVILEGE_DELETE_DESC = "删除";

    /**
     * 批处理权限编码
     */
    String PRIVILEGE_QUERY_CODE = "select";

    /**
     * 批处理权限描述
     */
    String PRIVILEGE_QUERY_DESC = "查询";

    /**
     * 批处理权限编码
     */
    String PRIVILEGE_BATCH_CODE = "batch";

    /**
     * 批处理权限描述
     */
    String PRIVILEGE_BATCH_DESC = "批量保存";

    /**
     * 导出权限编码
     */
    String PRIVILEGE_EXPORT_CODE = "export";

    /**
     * 导出权限描述
     */
    String PRIVILEGE_EXPORT_DESC = "导出";

    /**
     * 导入权限编码
     */
    String PRIVILEGE_IMPORT_CODE = "import";

    /**
     * 导入权限描述
     */
    String PRIVILEGE_IMPORT_DESC = "导入";

    /**
     * 中国时区TimeZone 
     * 可选方法toZoneId
     */
    TimeZone TIME_ZONE_CHINA = TimeZone.getTimeZone("GMT+08:00");

    /**
     * 中国时区ZoneOffset
     */
    ZoneOffset ZONE_OFFSET_CHINA = ZoneOffset.of("+08:00");

}
