package osgi.common.util.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * <p> a simple ioc container.
 * <p> could not resolve circular dependency. ensure there is no circular dependency by yourself.
 * <p> doesn't support multiple bean instances of the same class type.
 * <p> supports only singleton bean.
 * <p> implements only part of jsr330 'javax.inject' api annotations.
 *     support @Named on a type, @Inject on a field.
 * <p> the bean id by default will use the class name of a typed class, but will replace the first char to lower case; 
 * <p> if the value of @Named is set, it will be used as the bean id instead of the default class name. 
 * (id self define is meaningless now because this container supports singleton bean only and doesn't support primary bean and @Qualified annotation).
 * <p> keep the relationship of your bean dependency simple.
 * <p> doesn't use setter and getter and constructor with arguments, so it's a bit slower.
 * @author zhangchangchun
 * @Date 2022年7月4日
 */
class BeanInitializer {

    private final BundleContextManager bundleContextManager;

    /**
     * key:bean type name
     * value:resolving bean
     */
    Map<String, Object> resolvingBeans = new LinkedHashMap<>();

    /**
     * key:bean type name
     * value:injecting fields
     */
    Map<String, List<Field>> resolvingBean2UnresolvedDependencies = new LinkedHashMap<>();

    /**
     * key:bean type name
     * value:resolved bean
     */
    Map<String, Object> resolvedBeans = new LinkedHashMap<>();

    public BeanInitializer(BundleContextManager applicationContextManager) {
        this.bundleContextManager = applicationContextManager;
    }

    public void registBeans() throws BeanInitializeException {
        LinkedHashMap<String, Set<Class<?>>> localClasses = bundleContextManager.annotation2ClassMapper;
        Set<Class<?>> beansType = localClasses.get(Named.class.getName());
        if (beansType == null || beansType.isEmpty()) {
            return;
        }
        try {
            for (Class<?> beanType : beansType) {
                resolveBean(beanType);
            }
            if (!resolvedBeans.isEmpty()) {
                for (Entry<String, Object> entry : resolvedBeans.entrySet()) {
                    Object bean = entry.getValue();
                    String fullBeanClassName = entry.getKey();
                    String beanId = determinBeanId(bean, fullBeanClassName);
                    if (bundleContextManager.getBeanOfId(beanId) != null) {
                        throw new BeanInitializeException("duplicate bean id: " + beanId);
                    }
                    bundleContextManager.registBean(beanId, bean);
                }
            }
        } catch (Exception e) {
            throw new BeanInitializeException(e.getMessage());
        }

    }

    private String determinBeanId(Object bean, String fullBeanClassName) {
        String beanId = null;
        Named namedAnno = bean.getClass().getAnnotation(Named.class);
        String namedAnnoValue = namedAnno.value();
        if (namedAnnoValue != null && namedAnnoValue.length() > 0) {
            beanId = namedAnnoValue;
        } else {
            String beanClass = fullBeanClassName.substring(fullBeanClassName.lastIndexOf('.') + 1);
            char[] chars = beanClass.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            String defaultBeanId = new String(chars);
            beanId = defaultBeanId;
        }
        return beanId;
    }

    private void resolveBean(Class<?> beanType) throws NoSuchMethodException, ReflectiveOperationException {
        if (resolvedBeans.containsKey(beanType.getName())) {
            return;
        } else if (!resolvingBeans.containsKey(beanType.getName())) {
            Object bean = newInstanceByConstructorWithoutParam(beanType);
            Field[] fields = beanType.getDeclaredFields();
            boolean needAutoWired = false;
            if (fields.length > 0) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        needAutoWired = true;
                        break;
                    }
                }
            }
            if (!needAutoWired) {
                changeResolvedBeanStatusAndResolveWithReverseRecursion(bean);
                return;
            } else {
                resolveFields(bean);
            }
        } else {
            Object bean = resolvingBeans.get(beanType.getName());
            resolveFields(bean);
        }
    }

    private void changeResolvedBeanStatusAndResolveWithReverseRecursion(Object bean) throws ReflectiveOperationException {
        Class<?> beanType = bean.getClass();
        resolvedBeans.put(beanType.getName(), bean);
        resolvingBeans.remove(beanType.getName());
        resolvingBean2UnresolvedDependencies.remove(beanType.getName());
        resolveDependencyWithReverseRecursion(bean);
    }

    private void resolveFields(Object bean) throws ReflectiveOperationException, NoSuchMethodException {
        Class<?> beanType = bean.getClass();
        Field[] fields = beanType.getDeclaredFields();
        resolvingBeans.put(beanType.getName(), bean);
        List<Field> unresolvedFields = resolvingBean2UnresolvedDependencies.get(beanType.getName()) == null
                ? new ArrayList<>()
                : resolvingBean2UnresolvedDependencies.get(beanType.getName());
        resolvingBean2UnresolvedDependencies.put(beanType.getName(), unresolvedFields);
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Class<?> injectingType = field.getType();
            Object resolvedBean = resolvedBeans.get(injectingType.getName());
            if (resolvedBean != null) {
                try {
                    field.setAccessible(true);
                    field.set(bean, resolvedBean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ReflectiveOperationException(
                            "autowire failed on field " + field.getName() + " at class " + beanType.getName());
                }
            } else {
                unresolvedFields.add(field);
                // will this cause stackoverflow-exception if bean dependency relationship is complex?
                resolveBean(injectingType);
            }
        }
        if (unresolvedFields == null || unresolvedFields.isEmpty()) {//may already cleaned by reverseRecurseDependency process
            changeResolvedBeanStatusAndResolveWithReverseRecursion(bean);
        }
    }

    private void resolveDependencyWithReverseRecursion(Object bean) throws ReflectiveOperationException {
        for (Entry<String, List<Field>> entry : resolvingBean2UnresolvedDependencies.entrySet()) {
            String resolvingBeanTypeName = entry.getKey();
            Object resolvingBean = resolvingBeans.get(resolvingBeanTypeName);
            Class<?> class0 = bean.getClass();
            List<Field> dependencys = entry.getValue();
            Iterator<Field> it = dependencys.iterator();
            while (it.hasNext()) {
                Field field = it.next();
                if (field.getType().getName().equals(class0.getName())) {
                    try {
                        field.setAccessible(true);
                        field.set(resolvingBean, bean);
                        it.remove();
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new ReflectiveOperationException(
                                "autowire failed on field " + field.getName() + " at class " + class0.getName());
                    }
                }
            }
            if (dependencys.isEmpty()) {
                changeResolvedBeanStatusAndResolveWithReverseRecursion(resolvingBean);
            }
        }
    }

    private Object newInstanceByConstructorWithoutParam(Class<?> beanType)
            throws NoSuchMethodException, ReflectiveOperationException {
        Constructor<?> construct;
        try {
            construct = beanType.getConstructor(new Class[] {});
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException(
                    "bean " + beanType.getName() + " doesn't have a public constructor with no arguments");
        }
        Object bean;
        try {
            bean = construct.newInstance(new Object[] {});
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ReflectiveOperationException();
        }
        return bean;
    }
}
