package org.springbud.inject;

import lombok.extern.slf4j.Slf4j;
import org.springbud.core.ContainerBean;
import org.springbud.exceptions.FailToInjectException;
import org.springbud.inject.annotation.Autowired;
import org.springbud.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.Set;

@Slf4j
public class DependencyInjector {

    // The container of beans
    private final ContainerBean containerBean;

    // Fill the containerBean once constructed
    public DependencyInjector() {
        containerBean = ContainerBean.getInstance();
    }

    /**
     * Inject dependencies into beans
     */
    public void doIoC() {
        Set<Class<?>> classSet = containerBean.getClasses();
        if (classSet.isEmpty()) {
            log.warn("Class set is empty");
            return;
        }
        for (Class<?> clazz : classSet) {
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length == 0)
                continue;
            for (Field f : fields) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = f.getAnnotation(Autowired.class);
                    String autowiredValue = autowired.Value(); // The value in Autowired annotation
                    Class<?> fieldClass = f.getType();
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                    if (fieldValue == null) {
                        throw new FailToInjectException("Class " + fieldClass + " does not exist");
                    } else {
                        Object targetBean = containerBean.getBean(clazz);
                        ClassUtil.setField(f, targetBean, fieldValue, true);
                    }

                }
            }
        }
    }

    /**
     * Get the instance of given field
     * @param fieldClass the class of the field
     * @param autowiredValue the value in Autowired annotation
     * @return the value of the field
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {
        Object beanValue = containerBean.getBean(fieldClass);
        if (beanValue != null)
            return beanValue;
        else {
            Class<?> beanClass = getImplementClass(fieldClass, autowiredValue);
            if (beanClass != null) {
                return containerBean.getBean(beanClass);
            }else{
                return null;
            }
        }
    }

    /**
     * Get the implemented class of the super class or the interface
     * @param fieldClass the class of given field
     * @param autowiredValue the value in autowired annotation and used to find class if multiple implemented classes exist
     * @return The class of the implemented class of field class
     */
    private Class<?> getImplementClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = containerBean.getClassesBySuperClass(fieldClass);
        if (classSet.isEmpty())
            return null;
        else if (classSet.size() == 1)
            return classSet.iterator().next();
        else {
            if (autowiredValue.equals(""))
                throw new FailToInjectException("Autowired value is null but multiple dependencies exist");
            else {
                for (Class<?> clazz : containerBean.getClasses()) {
                    if (clazz.getSimpleName().equals(autowiredValue))
                        return clazz;
                }
                throw new FailToInjectException("Autowired value " + autowiredValue + " cannot be found but multiple dependencies exist");
            }
        }
    }
}
