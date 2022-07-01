package osgi.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class DateUtil.
 *
 * @author zhangchangchun
 * @Date 2022年1月18日
 */
public class DateUtil {

    /**
     * yyyy-MM-dd HH:mm:ss 数据库默认日期格式
     */
    public static final String FORMAT1 = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyyMMddHHmmss 常用于生成序列号
     */
    public static final String FORMAT2 = "yyyyMMddHHmmss";

    /**
     * yyyy-MM-dd'T'HH:mm:ss'Z' 标准时，支付相关接口要求
     */
    public static final String FORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * 获取yyyy-MM-dd HH:mm:ss 形式的sdf
     * @return
     */
    public static SimpleDateFormat getSdfInstance1() {
        return new SimpleDateFormat(FORMAT1);
    }

    /**
     * 获取yyyyMMddHHmmss 形式的sdf
     * @return
     */
    public static SimpleDateFormat getSdfInstance2() {
        return new SimpleDateFormat(FORMAT2);
    }

    /**
     * 获取yyyy-MM-dd'T'HH:mm:ss'Z' 形式的sdf
     * @return
     */
    public static SimpleDateFormat getSdfInstance3() {
        return new SimpleDateFormat(FORMAT3);
    }

    /**
     * 获取yyyy-MM-dd HH:mm:ss 形式的当前时刻
     * @return
     */
    public static String getCurrentTimeStr1() {
        return new SimpleDateFormat(FORMAT1).format(new Date());
    }

    /**
     * 获取yyyyMMddHHmmss 形式的当前时刻
     * @return
     */
    public static String getCurrentTimeStr2() {
        return new SimpleDateFormat(FORMAT2).format(new Date());
    }

    /**
     * 获取yyyy-MM-dd'T'HH:mm:ss'Z' 形式的当前时刻
     * @return
     */
    public static String getCurrentTimeStr3() {
        return new SimpleDateFormat(FORMAT3).format(new Date());
    }
}
