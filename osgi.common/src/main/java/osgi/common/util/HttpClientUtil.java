
package osgi.common.util;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {
    private static final String MIME_APPLICATION_JSON = "application/json";

    private static final String MIME_APPLICATION_FORM = "application/x-www-form-urlencoded";

    private static final String CHARSET_UTF_8 = "utf-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static HttpClient httpClient = null;

    private static String domain;

    private static String token;

    private static String appId;

    private static String appSecret;

    private HttpClientUtil() {
    }

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(50);
        connectionManager.setValidateAfterInactivity(5000);
        SocketConfig defaultSocketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoTimeout(30000).build();
        connectionManager.setDefaultSocketConfig(defaultSocketConfig);
        ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 60 * 1000;// 如果没有约定，则默认定义时长为60s
            }
        };
        httpClient = HttpClients.custom().setConnectionManager(connectionManager)
                .setKeepAliveStrategy(keepAliveStrategy).build();

        domain = PropertyUtil.getApplicationProp("application.domain");
        token = PropertyUtil.getApplicationProp("application.token");
        appId = PropertyUtil.getApplicationProp("application.appId");
        appSecret = PropertyUtil.getApplicationProp("application.appSecret");
    }

    /**
     * Do get rquest.（简单参数的get请求：无路径参数、上下文参数、头部参数）
     *
     * @param url          the url
     * @param parameterMap the parameter map 按接口显示定义的入参顺序放入的参数键值对有序集合
     * @return the string
     * @throws Exception the exception
     */
    public static String doSimpleGetRequest(String url, ArrayList<NameValuePair> parameterNameValuePairList)
            throws Exception {
        URI uri = null;
        ArrayList<Object> signParameters;
        if (parameterNameValuePairList != null && !parameterNameValuePairList.isEmpty()) {
            signParameters = new ArrayList<>(parameterNameValuePairList.size());
            for (NameValuePair entry : parameterNameValuePairList) {
                signParameters.add(entry.getValue());
            }
            uri = new URIBuilder(url).addParameters(parameterNameValuePairList).build();
        } else {
            signParameters = null;
            uri = new URI(url);
        }
        HttpGet request = new HttpGet(uri);
        String result = executeRequest(request, signParameters);
        return result;
    }

    /**
     * Do get rquest.（自己构造签名入参集合）
     *
     * @param url                        the url
     * @param parameterNameValuePairList 请求参数键值对,不要求有序添加
     * @param signParams                 the sign params 与服务端接口显示定义的入参顺序一至的入参值数组（
     *                                   可包括路径参数、上下文参数、头部参数、头部参数）， 用于签名
     * @return the string
     * @throws Exception the exception
     */
    public static String doGetRequest(String url, ArrayList<NameValuePair> parameterNameValuePairList,
            ArrayList<Object> signParams) throws Exception {
        URI uri = null;
        if (parameterNameValuePairList != null && !parameterNameValuePairList.isEmpty()) {
            uri = new URIBuilder(url).addParameters(parameterNameValuePairList).build();
        } else {
            uri = new URI(url);
        }
        HttpGet request = new HttpGet(uri);
        String result = executeRequest(request, signParams);
        return result;
    }

    /**
     * Do post request. （简单参数的post请求，接口入参无路径参数、上下文参数、头部参数，
     * 只有单对象基本类型数据对（类似Map<String,String>））
     *
     * @param url          the url
     * @param parameterMap 简单键值对的有序入参集合
     * @param asForm       是否以表单形式提交（是否需要对键值对进行url编码）
     * @return the string 接口响应字符串形式，可能是json
     * @throws Exception the exception
     */
    public static String doSimplePostRequest(String url, ArrayList<NameValuePair> parameterNameValuePairList,
            boolean asForm) throws Exception {
        HttpPost request = new HttpPost(url);
        ArrayList<Object> signParameters;
        if (parameterNameValuePairList != null && !parameterNameValuePairList.isEmpty()) {
            signParameters = new ArrayList<>(parameterNameValuePairList.size());
            HttpEntity entity = null;
            if (asForm) {
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameterNameValuePairList, CHARSET_UTF_8);
                formEntity.setContentType(MIME_APPLICATION_FORM);
                entity = formEntity;
                for (NameValuePair entry : parameterNameValuePairList) {
                    signParameters.add(URLEncoder.encode(entry.getValue(), CHARSET_UTF_8));
                }
            } else {
                Map<String, String> parameterMap = new TreeMap<>();
                for (NameValuePair entry : parameterNameValuePairList) {
                    parameterMap.put(entry.getName(), entry.getValue());
                    signParameters.add(entry.getValue());
                }
                String jsonString = JacksonUtil.getDefaultObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(parameterMap);
                StringEntity strEntity = new StringEntity(jsonString, CHARSET_UTF_8);
                strEntity.setContentType(MIME_APPLICATION_JSON);
                entity = strEntity;
            }
            request.setEntity(entity);
        } else {
            signParameters = null;
        }
        String result = executeRequest(request, signParameters);
        return result;
    }

    /**
     * Do post request.
     *
     * @param url        the url
     * @param jsonParam  json入参
     * @param signParams the sign params 与服务端接口显示定义的入参顺序一至的入参值数组（
     *                   可包括路径参数、上下文参数、头部参数、头部参数）， 用于签名
     * @return the string 接口响应字符串形式，可能是json
     * @throws Exception the exception
     */
    public static String doPostRequest(String url, String jsonParam, ArrayList<Object> signParams) throws Exception {
        HttpPost request = new HttpPost(url);
        if (StringUtils.isEmpty(jsonParam)) {
            jsonParam = "{}";
        }
        StringEntity entity = new StringEntity(jsonParam, CHARSET_UTF_8);
        entity.setContentType(MIME_APPLICATION_JSON);
        request.setEntity(entity);
        String result = executeRequest(request, signParams);
        return result;
    }

    /**
     * 添加自定义请求头，并对入参自动生成rsa签名
     * 
     * @param request            http请求
     * @param parametersSignList 签名参数有序（服务端接口定义入参顺序）集合
     * @return
     * @throws Exception
     */
    private static String executeRequest(HttpRequestBase request, ArrayList<Object> parametersSignList)
            throws Exception {
        long timestamp = System.currentTimeMillis();
        String sign = APISignUtil.sign(appId, timestamp, parametersSignList, appSecret);
        request.addHeader("z-domain", domain);
        request.addHeader("authorization", token);
        request.addHeader("appId", appId);
        request.addHeader("timestamp", String.valueOf(timestamp));
        request.addHeader("sign", sign);
        RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).build();
        request.setConfig(config);
        HttpResponse response = null;
        HttpEntity entity = null;
        try {
            response = httpClient.execute(request);
            if (response == null) {
                return "";
            }
            entity = response.getEntity();
            String result = EntityUtils.toString(entity, CHARSET_UTF_8);
            return result;
        } catch (Exception e) {
            LOGGER.error("do http request e:", e);
            throw e;
        } finally {
            if (response != null) {
                entity = response.getEntity();
                EntityUtils.consume(entity);
            }
        }
    }

}
