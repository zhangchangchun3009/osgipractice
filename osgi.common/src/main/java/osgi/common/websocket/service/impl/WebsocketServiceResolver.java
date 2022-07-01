
package osgi.common.websocket.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import osgi.common.util.ApplicationContextManager;
import osgi.common.websocket.service.interfaces.EndPoint;
import osgi.common.websocket.service.interfaces.MsgHandle;
import osgi.common.websocket.service.interfaces.WebsocketService;

public class WebsocketServiceResolver {

    private static Map<String, Map<String, Method>> pathMapper = new HashMap<String, Map<String, Method>>();

    private static Map<String, Object> pathBeanMapper = new HashMap<String, Object>();

    private void pathResolve() {
        Collection<?> services = ApplicationContextManager.getBeansWithAnnotation(WebsocketService.class);
        for (Object service : services) {
            Class<?> proxyClass = service.getClass();
            Class<?> originClass = null;
            try {
                originClass = Class.forName(proxyClass.getGenericSuperclass().getTypeName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Class<?> orginf = originClass.getInterfaces()[0];
            EndPoint endpoint = orginf.getAnnotation(EndPoint.class);
            if (endpoint == null) {
                continue;
            }
            String servicePath = endpoint.value();
            servicePath = normalizePath(servicePath);
            pathBeanMapper.put(servicePath, service);
            Map<String, Method> methodMap = new HashMap<>();
            Method[] methods = originClass.getDeclaredMethods();
            Map<Method, MsgHandle> methodAnnoMap = lookupAnnotationsOfImplMethodOnInterface(originClass,
                    MsgHandle.class);
            for (Method method : methods) {
                MsgHandle handle = methodAnnoMap.get(method);
                if (handle == null) {
                    continue;
                }
                String methodPath = handle.value();
                methodPath = normalizePath(methodPath);
                methodMap.put(methodPath, method);
            }
            pathMapper.put(servicePath, methodMap);
        }
    }

    /**
     * 处理消息
     * @param code 业务类型编码，暂没使用
     * @param path 请求地址，两段式斜杠分隔
     * @param param 请求参数，通常是json字符串
     * @return 返回值，通常是json
     */
    public String handleMessage(String code, String path, String param) {
        if (path == null || "".equals(path)) {
            return null;
        }
        String normalPath = normalizePath(path);
        String[] paths = normalPath.split("/");
        String servicePath = paths[0];
        String methodPath = paths[1];
        if (servicePath == null || methodPath == null) {
            return null;
        }
        Map<String, Method> methodMap = pathMapper.get(servicePath);
        Method method = methodMap.get(methodPath);
        Object bean = pathBeanMapper.get(servicePath);
        try {
            return (String) method.invoke(bean, param);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private <T extends Annotation> Map<Method, T> lookupAnnotationsOfImplMethodOnInterface(Class<?> implType,
            Class<T> interfaceAnno) {
        Map<Method, T> map = new HashMap<Method, T>();
        Method[] oms = implType.getDeclaredMethods();
        Class<?> interfaceType = implType.getInterfaces()[0];
        Method[] ims = interfaceType.getMethods();
        for (Method im : ims) {
            for (Method om : oms) {
                T anno = im.getDeclaredAnnotation(interfaceAnno);
                Class<?>[] iparamTypes = getParameterTypes(im);
                Class<?>[] oparamTypes = getParameterTypes(om);
                if (im.getName().equals(om.getName()) && Arrays.equals(iparamTypes, oparamTypes)) {
                    map.put(om, anno);
                }
            }
        }
        return map;
    }

    private Class<?>[] getParameterTypes(Method m) {
        Parameter[] params = m.getParameters();
        if (params == null || params.length == 0) {
            return new Class<?>[] {};
        }
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getType();
        }
        return paramTypes;
    }

    public void process() {
        pathResolve();
    }

    public int getOrder() {
        return 0;
    }

}
