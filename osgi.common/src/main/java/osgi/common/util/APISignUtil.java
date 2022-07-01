
package osgi.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APISignUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(APISignUtil.class);

    private static final long SIGN_EXPIRE = 1000 * 300;

    /**
     * 客户端根据自己的appId，时间戳，请求的接口入参生成签名.并将appId、timestamp、sign附在请求头中
     *
     * @param appId the app id 客户端应用id
     * @param timestamp the timestamp
     * @param parameters 接口入参，包括路径参数,可以是数组,按<Strong>接口形参顺序</Strong>装入数组或集合
     * @param appSecret the app secret 私钥
     * @return rsa 公钥加密密文
     * @throws Exception the exception
     */
    public static String sign(String appId, long timestamp, Object parameters, String appSecret) throws Exception {
        String objectFields = buildParamString(parameters);
        String str2Encrypt = "&" + appId + "&" + timestamp + "&" + objectFields;
        LOGGER.debug("str2Encrypt:" + str2Encrypt);
        return RSAUtil.sign(str2Encrypt.getBytes("UTF-8"), appSecret);
    }

    /**
     * 服务端使用公钥验签防参数篡改，接口有效期300s.
     *
     * @param appId the app id 客户端应用id，请求头appId
     * @param timestamp the timestamp 请求头 timestamp
     * @param parameters the parameters
     *            请求接口入参，包括路径参数,可以是数组,按<Strong>接口形参顺序</Strong>装入数组或集合
     * @param sign the sign 客户端应用的appId和客户端应用的私钥生成的签名串
     * @param appPublicKey the app public key 服务端保有的公钥
     * @return true, if successful
     * @throws Exception the exception
     */
    public static boolean verify(String appId, long timestamp, Object parameters, String sign, String appPublicKey)
            throws Exception {
        long now = System.currentTimeMillis();
        String objectFields = buildParamString(parameters);
        String content = "&" + appId + "&" + timestamp + "&" + objectFields;
        LOGGER.debug("content:" + content);
        return (now - timestamp < SIGN_EXPIRE) && RSAUtil.verify(content.getBytes("UTF-8"), appPublicKey, sign);
    }

    private static String buildParamString(Object parameters) {
        if (parameters == null) {
            return "";
        }
        String objectFields;
        if (parameters instanceof Object[]) {
            StringBuilder builder = new StringBuilder();
            for (Object o : ((Object[]) parameters)) {
                builder.append(getObjectFields(o));
            }
            objectFields = builder.toString();
        } else if (parameters instanceof List) {
            StringBuilder builder = new StringBuilder();
            ((List<?>) parameters).forEach(it -> builder.append(getObjectFields(it)));
            objectFields = builder.toString();
        } else if (parameters instanceof String || parameters instanceof Number || parameters instanceof Boolean) {
            objectFields = StringUtils.isEmpty(parameters.toString()) ? "" : parameters.toString() + "&";
        } else {
            objectFields = getObjectFields(parameters);
        }
        return objectFields;
    }

    private static String getObjectFields(Object object) {
        if (object == null) {
            return "";
        }
        if (object instanceof String || object instanceof Number || object instanceof Boolean) {
            return StringUtils.isEmpty(object.toString()) ? "" : object.toString() + "&";
        }
        final Field[] fields = object.getClass().getDeclaredFields();
        final TreeMap<String, Object> treeMap = new TreeMap<>();
        Stream.of(fields).map(Field::getName).forEach(it -> treeMap.put(it, getFieldValueByName(it, object)));
        final StringBuilder builder = new StringBuilder();
        treeMap.forEach((k, v) -> {
            if (!StringUtils.isEmpty(v.toString())) {
                builder.append(v);
            }
        });
        return builder.toString();
    }

    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            final Method method = o.getClass().getMethod(getter, new Class[] {});
            final Object value = method.invoke(o, new Object[] {});
            if (null == value) {
                return "";
            }
            if (value instanceof List) {
                return ((List<?>) value).stream().map(it -> {
                    if (it instanceof String || it instanceof Number || it instanceof Boolean) {
                        return StringUtils.isEmpty(it.toString()) ? "" : it.toString() + "&";
                    } else {
                        return getObjectFields(it);
                    }
                }).reduce((it1, it2) -> it1 + it2).get();
            } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return StringUtils.isEmpty(value.toString()) ? "" : value.toString() + "&";
            } else {
                return getObjectFields(value);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String getAppPublicKey(String appId) {
        if (PropertyUtil.getApplicationProp("application.appId").equalsIgnoreCase(appId)) {
            return PropertyUtil.getApplicationProp("application.appPublicKey");
        }
        return PropertyUtil.getApplicationProp(appId + ".appPublicKey");
    }
}
