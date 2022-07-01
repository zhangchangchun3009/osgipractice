
package osgi.common.util;

import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonUtil {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    private final static ObjectMapper defaultMapper = MapperHolder.defaultMapper;

    private static class MapperHolder {
        final static ObjectMapper defaultMapper = new ObjectMapper();
        static {
            defaultConfig(defaultMapper);
        }
    }

    /**
     * <p>返回默认配置的object mapper，通常应该调用这个方法以减少内存占用</p>
     * <p><strong>注意：千万不要获取引用后私自修改mapper配置！！！
     * 若有修改配置的需求应该调用<link>pers.zcc.scm.common.util.JacksonUtil.newObjectMapper()<link></strong></p>
     * <p>默认配置：
     * <p>1.在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSSZ
     * <p>2.在反序列化时忽略在 json 中存在但 Java 对象不存在的属性
     * <p>3.在序列化时忽略值为 null 的属性
     * <p>4.时区-中国北京
     * <p>Gets the default object mapper.
     *
     * @return the default object mapper
     */
    public static ObjectMapper getDefaultObjectMapper() {
        return defaultMapper;
    }

    /**
     * <p>返回一个新的mapper实例，已经使用了部分自定义配置，可以任意修改或覆盖配置
     * <p>建议在一个业务处理类里尽可能复用mapper对象
     * <p>New object mapper.
     *
     * @return the object mapper
     */
    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        defaultConfig(mapper);
        return mapper;
    }

    private static void defaultConfig(ObjectMapper mapper) {
        // 在反序列化时忽略在 json 中存在但 Java 对象不存在的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSSZ 
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //在序列化时忽略值为 null 的属性 
        mapper.setSerializationInclusion(Include.NON_NULL);

        // 指定时区
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        // 日期类型字符串处理
//        defaultMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT));
    }

}
