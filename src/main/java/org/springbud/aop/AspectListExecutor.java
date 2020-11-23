package org.springbud.aop;

import lombok.Getter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springbud.aop.support.AspectDefinition;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class AspectListExecutor implements MethodInterceptor {
    // The class need weaving
    private Class<?> targetClass;

    // The list of definition
    @Getter
    List<AspectDefinition> aspectDefinitionList;

    public AspectListExecutor(Class<?> targetClass, List<AspectDefinition> aspectDefinitions) {
        this.targetClass = targetClass;
        Collections.sort(aspectDefinitions, (AspectDefinition a1, AspectDefinition a2) -> {
            return a1.getOrderIndex() - a2.getOrderIndex();
        });
        this.aspectDefinitionList = aspectDefinitions;
    }

    /**
     * Execute hook methods when called to implement AOP
     * @param proxy the proxy instance
     * @param method the method need to call when doing AOP
     * @param args the arguments of the method need to call when doing AOP
     * @param methodProxy the methodProxy
     * @return the return value
     * @throws Throwable any throwable
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object returnValue = null;
        if (aspectDefinitionList.isEmpty())
            return returnValue;
        for (int i = 0;i < aspectDefinitionList.size();i ++) {
            aspectDefinitionList.get(i).getDefaultAspect().before(targetClass, method, args);
        }
        try {
            returnValue = methodProxy.invokeSuper(proxy, args);
            for (int i = aspectDefinitionList.size() - 1;i >= 0;i --) {
                returnValue = aspectDefinitionList.get(i).getDefaultAspect().afterRunning(targetClass, method, args, returnValue);
            }
        } catch (Exception e) {
            for (int i = aspectDefinitionList.size() - 1;i >= 0;i --) {
                aspectDefinitionList.get(i).getDefaultAspect().afterThrowing(targetClass, method, args, e);
            }
        } finally {
            for (int i = aspectDefinitionList.size() - 1;i >= 0;i --) {
                aspectDefinitionList.get(i).getDefaultAspect().after(targetClass, method, args);
            }
        }
        return returnValue;
    }
}
