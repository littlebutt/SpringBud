package org.springbud.aop;

import org.springbud.aop.annotation.Aspect;
import org.springbud.aop.annotation.Order;
import org.springbud.aop.support.AspectDefinition;
import org.springbud.aop.support.DefaultAspect;
import org.springbud.core.ContainerBean;

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

    /**
     * The core method for doing AOP
     */
    public void doAop(){
        Set<Class<?>> aspectSet = containerBean.getClassesByAnnotation(Aspect.class);
        if (aspectSet.isEmpty())
            return;
        List<AspectDefinition> aspectDefinitionList = packAspectDefinitionList(aspectSet);
        Set<Class<?>> classSet = containerBean.getClasses();
        for (Class<?> targetClass : classSet) {
            if (targetClass.isAnnotationPresent(Aspect.class))
                continue;
            List<AspectDefinition> roughMatchedAspectList = collectRoughMatchedAspectListForSpecificClass(aspectDefinitionList, targetClass);
            wrapIfNecessary(roughMatchedAspectList, targetClass);
        }
    }

    /**
     * Put them into AspectListExecutor and weave them by ProxyCreator
     * @param roughMatchedAspectList the rough matched classes
     * @param targetClass the class need to weave
     */
    private void wrapIfNecessary(List<AspectDefinition> roughMatchedAspectList, Class<?> targetClass) {
        if (roughMatchedAspectList == null || roughMatchedAspectList.isEmpty())
            return;
        AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, roughMatchedAspectList);
        Object porxBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);
        containerBean.addBean(targetClass, porxBean);
    }

    /**
     * Use the rough match method in the pointcut locator, which is in the AspectDefinition to do rough match for all possible classes
     * @param aspectDefinitionList the list of AspectDefinition
     * @param targetClass all possible classes
     * @return the filtered classes
     */
    private List<AspectDefinition> collectRoughMatchedAspectListForSpecificClass(List<AspectDefinition> aspectDefinitionList, Class<?> targetClass) {
        List<AspectDefinition> result = new ArrayList<>();
        if (aspectDefinitionList.isEmpty())
            return null;
        for (AspectDefinition aspectDefinition : aspectDefinitionList) {
            if (aspectDefinition.getPointcutLocator().rougnMatches(targetClass))
                result.add(aspectDefinition);
        }
        return result;
    }

    /**
     * Pack the class with @Aspect annotation into an AspectDefinition List
     * @param aspectSet the set of class with @Aspect annotation
     * @return the AspectDefinition List
     */
    private List<AspectDefinition> packAspectDefinitionList(Set<Class<?>> aspectSet) {
        List<AspectDefinition> aspectDefinitionList = new ArrayList<>();
        for (Class<?> targetClass : aspectSet) {
            if (verify(targetClass)) {
                Aspect aspectTag = targetClass.getAnnotation(Aspect.class);
                Order orderTag = targetClass.getAnnotation(Order.class);
                DefaultAspect defaultAspect = (DefaultAspect) containerBean.getBean(targetClass);
                PointcutLocator pointcutLocator = new PointcutLocator(aspectTag.pointcut());
                AspectDefinition aspectDefinition = new AspectDefinition(orderTag.value(),defaultAspect,pointcutLocator);
                aspectDefinitionList.add(aspectDefinition);
            }
        }
        return aspectDefinitionList;
    }

    private boolean verify(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(aspectClass);

    }
}
