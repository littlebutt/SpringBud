package org.springbud.aop;

import org.springbud.aop.annotation.Aspect;
import org.springbud.aop.annotation.Order;
import org.springbud.aop.support.AspectDefinition;
import org.springbud.aop.support.DefaultAspect;
import org.springbud.core.ContainerBean;
import org.springbud.exceptions.FailToWeaveException;

import java.lang.annotation.Annotation;
import java.util.*;

public class AspectWeaver {

    // Map for aspect tag with different aspects, since different @Aspect classes have different target classes.
    private final Map<Class<? extends Annotation>, List<AspectDefinition>> categorizedMap = new HashMap<>();

    // The bean container
    private final ContainerBean containerBean;

    public AspectWeaver() {
        containerBean = ContainerBean.getInstance();
    }

    public void doAop(){
        Set<Class<?>> aspectSet = containerBean.getClassesByAnnotation(Aspect.class);
        if (aspectSet.isEmpty())
            return;
        for (Class<?> aspectClass : aspectSet) {
            if (verify(aspectClass)){
                categorizeClass(categorizedMap, aspectClass);
            } else {
                throw new FailToWeaveException("Incorrect annotations or extension, value");
            }
        }
        if (categorizedMap.isEmpty())
            return;
        for (Class<? extends Annotation> category : categorizedMap.keySet()) {
            weaveByCategory(category, categorizedMap.get(category));
        }
    }

    /**
     * Weave by category
     * @param category @Aspect annotation (to get annotated classes)
     * @param aspectDefinitions the list of definition (to construct MethodInterceptor)
     */
    private void weaveByCategory(Class<? extends Annotation> category, List<AspectDefinition> aspectDefinitions) {
        Set<Class<?>> classSet = containerBean.getClassesByAnnotation(category);
        if (classSet.isEmpty())
            return;
        for (Class<?> targetClass : classSet) {
            AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass,aspectDefinitions);
            Object bean = ProxyCreator.createProxy(targetClass,aspectListExecutor);
            containerBean.addBean(targetClass, bean);
        }

    }

    /**
     * Fill the categorized map, AspectTag - Aspect Definition
     * The Aspect class is like:
     * / @Aspect(Controller.class)
     * / class aspectClass extends DefaultAspect {
     * /
     * / }
     * @param categorizedMap the categorized map
     * @param aspectClass class with @Aspect annotation
     */
    private void categorizeClass(Map<Class<? extends Annotation>, List<AspectDefinition>> categorizedMap, Class<?> aspectClass) {
        Aspect aspectTag = aspectClass.getAnnotation(Aspect.class);
        Order orderTag = aspectClass.getAnnotation(Order.class);
        DefaultAspect aspect = (DefaultAspect) containerBean.getBean(aspectClass);
        AspectDefinition aspectDefinition = new AspectDefinition(orderTag.value(),aspect);
        if (!categorizedMap.containsKey(aspectTag.value())){
            List<AspectDefinition> list = new ArrayList<>();
            list.add(aspectDefinition);
            categorizedMap.put(aspectTag.value(), list);
        } else {
            List<AspectDefinition> list = categorizedMap.get(aspectTag.value());
            list.add(aspectDefinition);
        }
    }

    private boolean verify(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(aspectClass) &&
                aspectClass.getAnnotation(Aspect.class).value() != Aspect.class;

    }
}
