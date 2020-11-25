package org.springbud.mvc.processor.impl;

import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import java.util.concurrent.RejectedExecutionException;

public class StaticResourceRequestProcessor implements RequestProcessor {

    public static final String DEFAULT_TOMCAT_SERVLET = "default";

    public static final String STATIC_RESOURCE_PREFIX = "/static/";

    private final RequestDispatcher dispatcher;

    public StaticResourceRequestProcessor(ServletContext servletContext) {
        dispatcher = servletContext.getNamedDispatcher(DEFAULT_TOMCAT_SERVLET);
        if (dispatcher == null) {
            throw new RejectedExecutionException("Cannot find default servlet");
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        String requestPath = requestProcessorChain.getRequestPath();
        if (requestPath.startsWith(STATIC_RESOURCE_PREFIX)) {
            dispatcher.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }
}
