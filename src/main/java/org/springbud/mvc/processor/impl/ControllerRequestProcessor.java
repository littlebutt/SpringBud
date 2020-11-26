package org.springbud.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springbud.core.ContainerBean;
import org.springbud.exceptions.RequestProcessorException;
import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.annotation.RequestMapping;
import org.springbud.mvc.annotation.RequestParam;
import org.springbud.mvc.annotation.ResponseBody;
import org.springbud.mvc.processor.RequestProcessor;
import org.springbud.mvc.render.ResultRender;
import org.springbud.mvc.render.impl.JsonResultRender;
import org.springbud.mvc.render.impl.ResourceNotFoundResultRender;
import org.springbud.mvc.render.impl.ViewResultRender;
import org.springbud.mvc.type.ControllerMethod;
import org.springbud.mvc.type.RequestPathDefinition;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.springbud.util.ConvertUtil.convertValue;
import static org.springbud.util.ConvertUtil.primitiveNull;

@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {

    // The map for RequestPathDefinition and ControllerMethod, RequestPathDefinition defines the path and the method, ControllerMethod includes Controller class
    private final Map<RequestPathDefinition, ControllerMethod> requestPathDefinitionControllerMethodMap = new HashMap<>();

    private final ContainerBean containerBean;

    /**
     * Fill the requestPathDefinitionControllerMethodMap once constructed.
     */
    public ControllerRequestProcessor() {
        this.containerBean = ContainerBean.getInstance();
        Set<Class<?>> requestMappingClasses = containerBean.getClassesByAnnotation(RequestMapping.class);
        initRequestPathDefinitionControllerMethodMap(requestMappingClasses);
    }

    /**
     * Traverse all layers of RequestMapping annotated classes and fill requestPathDefinitionControllerMethodMap.
     *
     * @param requestMappingClasses All the classes in the container with RequestMapping annotation.
     */
    private void initRequestPathDefinitionControllerMethodMap(Set<Class<?>> requestMappingClasses) {
        if (requestMappingClasses.isEmpty())
            return;
        for (Class<?> requestMappingClass : requestMappingClasses) {
            RequestMapping requestMappingTag = requestMappingClass.getAnnotation(RequestMapping.class);
            String basePath = requestMappingTag.value();
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            Method[] requestMappingMethods = requestMappingClass.getDeclaredMethods();
            if (requestMappingMethods.length == 0)
                continue;
            for (Method requestMappingMethod : requestMappingMethods) {
                if (requestMappingMethod.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMappingMethodTag = requestMappingMethod.getAnnotation(RequestMapping.class);
                    String methodPath = requestMappingMethodTag.value();
                    if (!methodPath.startsWith("/")) {
                        methodPath = "/" + methodPath;
                    }
                    String url = basePath + methodPath;
                    Parameter[] parameters = requestMappingMethod.getParameters();
                    Map<String, Class<?>> paramMap = new HashMap<>();
                    if (parameters != null && parameters.length > 0) {
                        for (Parameter parameter : parameters) {
                            RequestParam requestParamTag = parameter.getAnnotation(RequestParam.class);
                            if (requestParamTag == null)
                                throw new RequestProcessorException("Parameters must have @RequestParam");
                            paramMap.put(requestParamTag.value(), parameter.getType());
                        }
                    }

                    RequestPathDefinition requestPathDefinition = new RequestPathDefinition(url, String.valueOf(requestMappingTag.method()));
                    if (this.requestPathDefinitionControllerMethodMap.containsKey(requestPathDefinition))
                        log.warn("{} has already mapped", url);
                    ControllerMethod controllerMethod = new ControllerMethod(requestMappingClass, requestMappingMethod, paramMap);
                    this.requestPathDefinitionControllerMethodMap.put(requestPathDefinition, controllerMethod);

                }
            }
        }
    }

    /**
     * Called when processing every requests from clients
     *
     * @param requestProcessorChain the job chain for processing requests
     * @return true if taking the current job
     * @throws Exception any exception
     */
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        String requestMethod = requestProcessorChain.getRequestMethod();
        String requestPath = requestProcessorChain.getRequestPath();
        ControllerMethod controllerMethod = requestPathDefinitionControllerMethodMap.get(new RequestPathDefinition(requestPath, requestMethod));
        if (controllerMethod == null) {
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(requestMethod, requestPath));
            return false;
        }
        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest());
        setResultRender(result, controllerMethod, requestProcessorChain);
        return true;
    }

    private void setResultRender(Object result, ControllerMethod controllerMethod, RequestProcessorChain requestProcessorChain) {
        if (result == null)
            return;
        ResultRender resultRender;
        if (controllerMethod.getInvokeMethod().isAnnotationPresent(ResponseBody.class)) {
            resultRender = new JsonResultRender(result);
        } else {
            resultRender = new ViewResultRender(result);
        }
        requestProcessorChain.setResultRender(resultRender);
    }

    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request) {
        // Map for Key-Value in URLs
        Map<String, String> requestParamMap = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v.length > 0)
                requestParamMap.put(k, v[0]);
        });

        List<Object> methodParams = new ArrayList<>();
        controllerMethod.getMethodParams().forEach((k, v) -> {
            String requestValue = requestParamMap.get(k);
            Object value;
            if (requestValue == null)
                value = primitiveNull(v);
            else
                value = convertValue(v, requestValue);
            methodParams.add(value);
        });

        Object controller = containerBean.getBean(controllerMethod.getControllerClass());
        Method invokeMethod = controllerMethod.getInvokeMethod();
        invokeMethod.setAccessible(true);
        Object result;
        try {
            if (methodParams.size() == 0)
                result = invokeMethod.invoke(controller);
            else
                result = invokeMethod.invoke(controller, methodParams.toArray());
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                throw new RequestProcessorException(((InvocationTargetException) e).getTargetException().getMessage());
            else
                throw new RequestProcessorException(e.getMessage());
        }
        return result;
    }
}
