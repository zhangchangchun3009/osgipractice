package osgi.common.util.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * <p> a simple ioc container.
 * <p> could not resolve circular dependency. ensure there is no circular dependency by yourself.
 * <p> supports only singleton bean.
 * <p> implements only part of jsr330 'javax.inject' api annotations.
 *     support @Named on a type, @Inject on a field, @Named("xx") on a field.
 * <p> the bean id by default will use the class name of a typed class, but will replace the first char to lower case; 
 * <p> if the value of @Named is set, it will be used as the bean id instead of the default class name. 
 * <p> keep the relationship of your bean dependency simple.
 * <p> doesn't use setter and getter and constructor with arguments, so it's a bit slow.
 * @author zhangchangchun
 * @Date 2022年7月4日
 */
class BeanInitializer {

    private final BundleContextManager bundleContextManager;

    /**
     * key:beanId
     * value:resolving bean
     */
    Map<String, Object> resolvingBeans = new LinkedHashMap<>();

    /**
     * key:resolving bean Id
     * value:dependended field entries, entry key is bean id of the field type.
     */
    Map<String, Map<String, Field>> resolvingBean2UnresolvedDependencies = new LinkedHashMap<>();

    /**
     * key:beanId
     * value:resolved bean
     */
    Map<String, Object> resolvedBeans = new LinkedHashMap<>();

    /**
     * key:beanId
     * value:auto Construct Beans
     */
    Map<String, Object> autoConstructBeans = new LinkedHashMap<>();

    public BeanInitializer(BundleContextManager applicationContextManager) {
        this.bundleContextManager = applicationContextManager;
        resolvedBeans.putAll(applicationContextManager.beanMap);
    }

    public void registLocalBeans() throws BeanInitializeException {
        LinkedHashMap<String, Set<Class<?>>> localClasses = bundleContextManager.annotation2ClassMapper;
        Set<Class<?>> beansType = localClasses.get(Named.class.getName());
        if (beansType == null || beansType.isEmpty()) {
            return;
        }
        try {
            for (Class<?> beanType : beansType) {
                if (beanType.isInterface()) {
                    continue;
                }
                Class<?> determinedBeanType = determinLocalBeanType(beanType);
                String beanId = determinLocalBeanId(determinedBeanType);
                resolveBean(beanType, beanId);
            }
            if (!autoConstructBeans.isEmpty()) {
                for (Entry<String, Object> entry : autoConstructBeans.entrySet()) {
                    Object bean = entry.getValue();
                    String beanId = entry.getKey();
                    if (bundleContextManager.getBeanOfId(beanId) != null) {
                        throw new BeanInitializeException("duplicate bean id: " + beanId);
                    }
                    bundleContextManager.registBean(beanId, bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanInitializeException(e.getMessage());
        }

    }

    private String determinLocalBeanId(Class<?> beanType) {
        Named namedAnno = beanType.getAnnotation(Named.class);
        String beanName = namedAnno.value();
        String beanId = null;
        if (beanName != null && beanName.length() > 0) {
            beanId = beanName;
        } else {
            String fullBeanClassName = beanType.getName();
            String defaultBeanId = convertFullClassNameToBeanId(fullBeanClassName);
            beanId = defaultBeanId;
        }
        return beanId;
    }

    private String convertFullClassNameToBeanId(String fullBeanClassName) {
        String beanClass = fullBeanClassName.substring(fullBeanClassName.lastIndexOf('.') + 1);
        char[] chars = beanClass.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        String defaultBeanId = new String(chars);
        return defaultBeanId;
    }

    /**
     * types tagged by @Named can't be interface, but fields tagged by @Inject can be interface, and it's the recommended way. 
     *
     * @param beanType determined bean type
     * @param beanId determined bean id @Notnull
     * @throws NoSuchMethodException the no such method exception
     * @throws ReflectiveOperationException the reflective operation exception
     */
    private void resolveBean(Class<?> beanType, String beanId)
            throws NoSuchMethodException, ReflectiveOperationException {
        if (beanId != null && beanId.length() > 0) {
            if (resolvedBeans.containsKey(beanId)) {
                return;
            } else if (resolvingBeans.containsKey(beanId)) {
                Object bean = resolvingBeans.get(beanId);
                resolveFields(bean, beanId);
                return;
            }
        }
        // need new bean, check id first.
        if (resolvedBeans.keySet().contains(beanId)) {
            throw new BeanInitializeException("duplicate bean id: " + beanId);
        }
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
            changeResolvedBeanStatusAndResolveWithReverseRecursion(bean, beanId);
            return;
        } else {
            resolveFields(bean, beanId);
        }
    }

    private Class<?> determinLocalBeanType(Class<?> describingBeanType) {
        Class<?> determinedBeanType = describingBeanType;
        if (describingBeanType.isInterface()) {
            Set<Class<?>> beanImplTypesOfInterface = bundleContextManager.interface2ClassMapper
                    .get(describingBeanType.getName());
            if (beanImplTypesOfInterface == null || beanImplTypesOfInterface.isEmpty()) {
                throw new BeanInitializeException(
                        "bean interface type " + describingBeanType.getName() + " has no implementation");
            } else if (beanImplTypesOfInterface.size() > 1) {
                throw new BeanInitializeException(
                        "bean interface type " + describingBeanType.getName() + " has more than one implementation");
            } else {
                determinedBeanType = beanImplTypesOfInterface.iterator().next();
            }
        }
        return determinedBeanType;
    }

    private void changeResolvedBeanStatusAndResolveWithReverseRecursion(Object bean, String beanId)
            throws ReflectiveOperationException {
        autoConstructBeans.put(beanId, bean);
        resolvedBeans.put(beanId, bean);
        resolvingBeans.remove(beanId);
        resolvingBean2UnresolvedDependencies.remove(beanId);
        resolveDependencyWithReverseRecursion(bean, beanId);
    }

    private void resolveFields(Object bean, String beanId) throws ReflectiveOperationException, NoSuchMethodException {
        Class<?> beanType = bean.getClass();
        Field[] fields = beanType.getDeclaredFields();
        resolvingBeans.put(beanId, bean);
        Map<String, Field> unresolvedFieldBeanIds = resolvingBean2UnresolvedDependencies.get(beanId) == null
                ? new HashMap<>()
                : resolvingBean2UnresolvedDependencies.get(beanId);
        resolvingBean2UnresolvedDependencies.put(beanId, unresolvedFieldBeanIds);
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            String determinedInjectingBeanId = determinFieldBeanId(field);
            Object resolvedBean = resolvedBeans.get(determinedInjectingBeanId);
            if (resolvedBean != null) {
                try {
                    field.setAccessible(true);
                    field.set(bean, resolvedBean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ReflectiveOperationException(
                            "autowire failed on field " + field.getName() + " at class " + beanType.getName());
                }
            } else {
                unresolvedFieldBeanIds.put(determinedInjectingBeanId, field);
                // will this cause stackoverflow-exception if bean dependency relationship is complex?
                Class<?> determinedInjectingType = determinFieldBeanType(field);
                resolveBean(determinedInjectingType, determinedInjectingBeanId);
            }
        }
        if (unresolvedFieldBeanIds == null || unresolvedFieldBeanIds.isEmpty()) {//may already cleaned by reverseRecurseDependency process
            changeResolvedBeanStatusAndResolveWithReverseRecursion(bean, beanId);
        }
    }

    private Class<?> determinFieldBeanType(Field field) {
        return determinLocalBeanType(field.getType()); // outer beans are resolved, this field can't be outer bean.
    }

    private String determinFieldBeanId(Field field) {
        String beanId;
        Named fieldNamedAnno = field.getAnnotation(Named.class);
        if (fieldNamedAnno != null && fieldNamedAnno.value() != null && fieldNamedAnno.value().length() > 0) {
            beanId = fieldNamedAnno.value();
        } else {
            Class<?> fieldType = field.getType();
            if (!fieldType.isInterface()) {
                beanId = convertFullClassNameToBeanId(field.getType().getName());
            } else {
                Map<String, Object> valiableOuterBeans = new HashMap<>();
                for (Entry<String, Object> entry : bundleContextManager.beanMap.entrySet()) {
                    String entryBeanId = entry.getKey();
                    Object entryBean = entry.getValue();
                    if (fieldType.isInstance(entryBean)) {
                        valiableOuterBeans.put(entryBeanId, entryBean);
                    }
                }
                if (valiableOuterBeans.size() > 1) {
                    throw new BeanInitializeException("more than one bean of type " + fieldType.getName()
                            + " are avaliable, use @Named(value=beanName) annotation to qualify one");
                } else if (valiableOuterBeans.size() == 1) {
                    Entry<String, Object> determinedEntry = valiableOuterBeans.entrySet().iterator().next();
                    beanId = determinedEntry.getKey(); // we can also find bean here.
                } else {
                    Set<Class<?>> localClassesOfType = bundleContextManager.interface2ClassMapper
                            .get(fieldType.getName());
                    if (localClassesOfType == null || localClassesOfType.size() == 0) {
                        throw new BeanInitializeException("no bean of type " + fieldType.getName() + " avaliable");
                    } else if (localClassesOfType.size() > 1) {
                        throw new BeanInitializeException("more than one bean of type " + fieldType.getName()
                                + " are avaliable, use @Named(value=beanName) annotation to qualify one");
                    } else {
                        Class<?> determinedType = localClassesOfType.iterator().next();
                        Named named = determinedType.getAnnotation(Named.class);
                        if (named == null || named.value() == null || named.value().length() == 0) {
                            beanId = convertFullClassNameToBeanId(determinedType.getName());
                        } else {
                            beanId = named.value();
                        }
                    }
                }
            }
        }
        return beanId;
    }

    private void resolveDependencyWithReverseRecursion(Object bean, String beanId) throws ReflectiveOperationException {
        for (Entry<String, Map<String, Field>> entry : resolvingBean2UnresolvedDependencies.entrySet()) {
            String resolvingBeanTypeName = entry.getKey();
            Object resolvingBean = resolvingBeans.get(resolvingBeanTypeName);
            Class<?> class0 = bean.getClass();
            Map<String, Field> dependencys = entry.getValue();
            Iterator<Entry<String, Field>> it = dependencys.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Field> fieldEntry = it.next();
                String dependentBeanId = fieldEntry.getKey();
                Field field = fieldEntry.getValue();
                if (dependentBeanId.equals(beanId)) {
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
        }
    }

    private Object newInstanceByConstructorWithoutParam(Class<?> beanType)
            throws NoSuchMethodException, ReflectiveOperationException {
        Constructor<?> construct;
        try {
            construct = beanType.getConstructor(new Class[] {});
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException(
                    "bean type " + beanType.getName() + " doesn't have a public constructor with no arguments");
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
