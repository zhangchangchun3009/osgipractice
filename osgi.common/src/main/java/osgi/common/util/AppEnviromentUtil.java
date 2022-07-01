package osgi.common.util;

/**
 * <p>The Class AppEnviromentUtil.
 * <p>this class is used to decide key value from all possible sources,eg jvm args, host environment, local property file;
 * <p>values of the same key is decided by the priority(temporary turns)--  jvm args > host environment > local property file.
 *    higher priority configuration will be used as last value
 * @author zhangchangchun
 * @Date 2022年6月13日
 */
public class AppEnviromentUtil {

    public static String getString(String key, String defaultValue) {
        return isStringNotNull(System.getProperty(key)) ? System.getProperty(key)
                : isStringNotNull(System.getenv(key)) ? System.getenv(key)
                        : isStringNotNull(PropertyUtil.getApplicationProp(key)) ? PropertyUtil.getApplicationProp(key)
                                : defaultValue;
    }

    public static int getInteger(String key, int defaultValue) {
        String value = getString(key, null);
        return isStringNotNull(value) ? Integer.parseInt(value) : defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        String value = getString(key, null);
        return isStringNotNull(value) ? Long.parseLong(value) : defaultValue;
    }

    public static String getActiveProfile() {
        return getString("spring.profiles.active", null);
    }

    private static boolean isStringNotNull(String str) {
        return str != null && !"".equals(str);
    }

}
