package osgi.common.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *  we may not scan package of another bundle under osgi framework.
 * @author zhangchangchun
 * @Date 2022年7月1日
 */
public class ApplicationContextManager {

    private static final String BASE_PACKAGE = "osgi";

    private static final LinkedHashMap<String, Class<?>> classMapper = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Set<Class<?>>> interface2ClassMapper = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Set<Class<?>>> annotation2ClassMapper = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Object> beanMap = new LinkedHashMap<>();

    static {
        PackageScanner scanner = new PackageScanner(ApplicationContextManager.class.getClassLoader(), BASE_PACKAGE,
                true, null, null);
        try {
            for (Class<?> class1 : scanner.doScanAllClasses()) {
                classMapper.put(class1.getName(), class1);
                if (class1.isLocalClass()) {
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
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registBean(String beanId, Object bean) {
        beanMap.put(beanId, bean);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanOfId(String beanId) {
        return (T) beanMap.get(beanId);
    }

    public static <T> Set<T> getBeansOfType(Class<T> interface0) {
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

    public static <T> Set<T> getBeansWithAnnotation(Class<?> annotation0) {
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

    @SuppressWarnings("unchecked")
    private static <T> Set<T> findRegisterBeanOfType(Class<?> type) {
        Set<T> beansOfType = new LinkedHashSet<>();
        for (Object bean : beanMap.values()) {
            if (type.isInstance(bean)) {
                beansOfType.add((T) bean);
            }
        }
        return beansOfType;
    }

}
