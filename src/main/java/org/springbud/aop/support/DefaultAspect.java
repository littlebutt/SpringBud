package org.springbud.aop.support;

import java.lang.reflect.Method;

public abstract class DefaultAspect {

    /**
     * The before hook of AOP
     * @param target the class weaved to do aspect
     * @param method the called method when doing aspect
     * @param args the arguments for the called method when doing aspect
     * @throws Throwable any throwable
     */
    public void before(Class<?> target, Method method, Object[] args) throws Throwable {

    }

    /**
     * The afterrunning hook of AOP
     * @param target the class weaved to do aspect
     * @param method the called method when doing aspect
     * @param args the arguments for the called method when doing aspect
     * @param returnValue the return value of the called method when doing aspect
     * @return returnValue
     * @throws Throwable any throwable
     */
    public Object afterRunning(Class<?> target, Method method, Object[] args, Object returnValue) throws Throwable {
        return returnValue;
    }

    /**
     * The afterthrowing hook of AOP
     * @param target the class weaved to do aspect
     * @param method the called method when doing aspect
     * @param args the arguments for the called method when doing aspect
     * @param throwable any throwable from the called method when doing aspect
     * @throws Throwable any throwable
     */
    public void afterThrowing(Class<?> target, Method method, Object[] args, Throwable throwable) throws Throwable{

    }

    /**
     * The after hook of AOP
     * @param target the class weaved to do aspect
     * @param method the called method when doing aspect
     * @param args the arguments for the called method when doing aspect
     * @throws Throwable any throwable
     */
    public void after(Class<?> target, Method method, Object[] args) throws Throwable {

    }
}
