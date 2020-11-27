package org.springbud.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springbud.aop.annotation.Aspect;
import org.springbud.core.annotation.Component;
import org.springbud.core.annotation.Controller;
import org.springbud.core.annotation.Repository;
import org.springbud.core.annotation.Service;
import org.springbud.mvc.annotation.RequestMapping;
import org.springbud.mvc.annotation.RequestParam;
import org.springbud.mvc.annotation.ResponseBody;
import org.springbud.orm.annotations.Column;
import org.springbud.orm.annotations.DatabaseConfigurer;
import org.springbud.orm.annotations.Id;
import org.springbud.orm.annotations.Table;
import org.springbud.util.ClassUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainerBean {

    // The map used to contain beans in given scope
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    // The list used to contain inner annotations
    private final static List<Class<? extends Annotation>> BEAN_ANNOTATION = Arrays.asList(Component.class,
            Controller.class,
            Repository.class,
            Service.class,
            Aspect.class,
            RequestMapping.class,
            RequestParam.class,
            ResponseBody.class,
            DatabaseConfigurer.class,
            Id.class,
            Table.class,
            Column.class);

    // flag for if the bean container has been loaded
    private static boolean loaded = false;

    /**
     * Use Enum to implement the singleton of the bean container,
     * which can defend the attack from reflection, serializable methods, and default constructor.
     */
    private enum ContainerBeanHolder {
        HOLDER;
        private final ContainerBean containerBean;
        ContainerBeanHolder(){
            containerBean = new ContainerBean();
        }
    }

    /**
     * The method used to get the instance of the bean container
     * @return ContainerBean
     */
    public static ContainerBean getInstance() {
        return ContainerBeanHolder.HOLDER.containerBean;
    }

    /**
     * To check if the bean container is loaded
     * @return true if loaded
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Get the size of the bean container
     * @return int
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * load bean in given scope into the inner bean map
     * @param packageName The scope of beans
     */
    public void loadBeans(String packageName) {
        if (loaded) {
            log.warn("Bean container has been loaded");
            return;
        }
        Set<Class<?>> classSet = null;
        try {
            classSet = ClassUtil.extractPackageClasses(packageName);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if (classSet == null || classSet.isEmpty()) {
            log.warn("Class set is empty");
            return;
        }
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> ant : BEAN_ANNOTATION) {
                if (clazz.isAnnotationPresent(ant)) {
                    beanMap.put(clazz, ClassUtil.getInstance(clazz, true));
                }
            }
        }
        loaded = true;
    }

    /**
     * Add bean into the bean map
     * @param clazz the class of the bean
     * @param instance the instance of the bean
     * @return the instance of the bean
     */
    public Object addBean(Class<?> clazz, Object instance) {
        return beanMap.put(clazz, instance);
    }

    /**
     * Get bean from the bean map
     * @param clazz the clazz of the bean
     * @return the instance of the bean
     */
    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }

    /**
     * Remove bean from the bean map
     * @param clazz the clazz of the bean
     * @return the instance of the bean
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * Get the class set of beans
     * @return the class set of beans
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * Get the set of beans
     * @return the set of beans
     */
    public Set<Object> getBeans() {
        return new HashSet<Object>(beanMap.values());
    }

    /**
     * Get classes of beans by annotation
     * @param annotation the annotation of beans
     * @return the class set of beans
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation) {
        if (beanMap.keySet().isEmpty()) {
            log.warn("Class set is empty");
            return null;
        }
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> clazz : beanMap.keySet()) {
            if (clazz.isAnnotationPresent(annotation)) {
                classes.add(clazz);
            }
        }
        return classes.size() > 0 ? classes :null;
    }

    /**
     * Get classes of beans by super class (excluded)
     * @param superClass the super class or interface of the class of beans
     * @return the class set of beans
     */
    public Set<Class<?>> getClassesBySuperClass(Class<?> superClass) {
        if (beanMap.keySet().isEmpty()) {
            log.warn("Class set is empty");
            return null;
        }
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> clazz : beanMap.keySet()) {
            if (superClass.isAssignableFrom(clazz) && !clazz.equals(superClass)) {
                classes.add(clazz);
            }
        }
        return classes.size() > 0 ? classes :null;
    }

    /**
     * Get the set of beans by annotation
     * @param annotation the annotation of beans
     * @return the set of beans
     */
    public Set<Object> getBeansByAnnotation(Class<? extends Annotation> annotation) {
        if (beanMap.keySet().isEmpty()) {
            log.warn("Class set is empty");
            return null;
        }
        Set<Object> beans = new HashSet<>();
        for (Class<?> clazz : beanMap.keySet()) {
            if (clazz.isAnnotationPresent(annotation)) {
                Object bean = beanMap.get(clazz);
                beans.add(bean);
            }
        }
        return beans.size() > 0 ? beans : null;
    }
}
