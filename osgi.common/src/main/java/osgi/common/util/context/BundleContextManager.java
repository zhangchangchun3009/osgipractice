/*
 * Copyright 2021-2029 tiangong All Rights Reserved
 */
package osgi.common.util.context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *  <p> we may not scan package of another bundle under osgi framework.
 *  <p> every bundle should new its own instance
 * @author zhangchangchun
 * @Date 2022年7月1日
 */
public class BundleContextManager {

    private String BASE_PACKAGE = "osgi";

    /**
     * <p> The class Mapper of classes in the BASE_PACKAGE. 
     * <p> the key is class name,value is java class type.
     * */
    private final LinkedHashMap<String, Class<?>> classMapper = new LinkedHashMap<>();

    /**
     * classify scanned classes by interface name
     */
    final LinkedHashMap<String, Set<Class<?>>> interface2ClassMapper = new LinkedHashMap<>();

    /**
     * classify scanned classes by annotation name
     */
    final LinkedHashMap<String, Set<Class<?>>> annotation2ClassMapper = new LinkedHashMap<>();

    /**
     * bean container, the key is unique id,value is bean
     */
    private final LinkedHashMap<String, Object> beanMap = new LinkedHashMap<>();

    /**
     * key is bundle name, value is context.
     */
    private static final Hashtable<String, BundleContextManager> instanceMap = new Hashtable<>();

    public BundleContextManager(String basePackage) {
        this.BASE_PACKAGE = basePackage != null ? basePackage : BASE_PACKAGE;
        scanPackage();
        registBeans();
    }

    private void registBeans() {
        new BeanInitializer(this).registBeans();
    }

    private void scanPackage() {
        PackageScanner scanner = new PackageScanner(BundleContextManager.class.getClassLoader(), BASE_PACKAGE,
                true, null, null);
        try {
            for (Class<?> class1 : scanner.doScanAllClasses()) {
                classMapper.put(class1.getName(), class1);
                if (class1.isAnnotation() || class1.isInterface() || class1.isSynthetic() || class1.isEnum()
                        || class1.isMemberClass() || class1.isAnonymousClass() || class1.isLocalClass()
                        || Exception.class.isInstance(class1)) {
                    continue;
                }
                Class<?>[] interfaces = class1.getInterfaces();
                if (interfaces != null && interfaces.length > 0) {
                    for (Class<?> interface0 : interfaces) {
                        if (interface2ClassMapper.containsKey(interface0.getName())) {
                            Set<Class<?>> setOfInterface0 = interface2ClassMapper.get(interface0.getName());
                            setOfInterface0.add(class1);
                        } else {
                            Set<Class<?>> setOfInterface0 = new LinkedHashSet<>();
                            setOfInterface0.add(class1);
                            interface2ClassMapper.put(interface0.getName(), setOfInterface0);
                        }
                    }
                }
                Annotation[] annotations = class1.getDeclaredAnnotations();
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation0 : annotations) {
                        String annoName = annotation0.annotationType().getName();
                        if (annotation2ClassMapper.containsKey(annoName)) {
                            Set<Class<?>> setOfAnnotation0 = annotation2ClassMapper.get(annoName);
                            setOfAnnotation0.add(class1);
                        } else {
                            Set<Class<?>> setOfAnnotation0 = new LinkedHashSet<>();
                            setOfAnnotation0.add(class1);
                            annotation2ClassMapper.put(annoName, setOfAnnotation0);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registContext(String bundleName, BundleContextManager bundleContext) {
        instanceMap.put(bundleName, bundleContext);
    }

    public static BundleContextManager getContextByBundleName(String bundleName) {
        return instanceMap.get(bundleName);
    }

    void registBean(String beanId, Object bean) {
        beanMap.put(beanId, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBeanOfId(String beanId) {
        return (T) beanMap.get(beanId);
    }

    public <T> Set<T> getBeansOfType(Class<T> interface0) {
        Set<Class<?>> typeOfInterface0 = interface2ClassMapper.get(interface0.getName());
        if (typeOfInterface0 == null) {
            return Collections.emptySet();
        }
        Set<T> beansOfInterface0 = new LinkedHashSet<>();
        for (Class<?> type : typeOfInterface0) {
            Set<T> beansOfType = findRegisterBeanOfType(type);
            beansOfInterface0.addAll(beansOfType);
        }
        return beansOfInterface0;
    }

    public <T> Set<T> getBeansWithAnnotation(Class<?> annotation0) {
        Set<Class<?>> typeOfAnnotation0 = annotation2ClassMapper.get(annotation0.getName());
        if (typeOfAnnotation0 == null) {
            return Collections.emptySet();
        }
        Set<T> beansOfAnnotation0 = new LinkedHashSet<>();
        for (Class<?> type : typeOfAnnotation0) {
            Set<T> beansOfType = findRegisterBeanOfType(type);
            beansOfAnnotation0.addAll(beansOfType);
        }
        return beansOfAnnotation0;
    }

    /**
     * <p> find instance of the class type in the bean container
     * @param <T>
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> Set<T> findRegisterBeanOfType(Class<?> type) {
        Set<T> beansOfType = new LinkedHashSet<>();
        for (Object bean : beanMap.values()) {
            if (type.isInstance(bean)) {
                beansOfType.add((T) bean);
            }
        }
        return beansOfType;
    }

}
