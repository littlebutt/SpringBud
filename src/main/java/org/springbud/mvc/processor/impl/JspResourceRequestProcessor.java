package org.springbud.mvc.processor.impl;

import org.springbud.exceptions.RequestProcessorException;
import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

public class JspResourceRequestProcessor implements RequestProcessor {
    public static final String JSP_SERVLET = "jsp";

    public static final String JSP_RESOURCE_PREFIX = "/template/";

    private final RequestDispatcher requestDispatcher;

    public JspResourceRequestProcessor(ServletContext servletContext) {
        requestDispatcher = servletContext.getNamedDispatcher(JSP_SERVLET);
        if (requestDispatcher == null) {
            throw new RequestProcessorException("Cannot find JSP servlet");
        }
    }
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        String requestPath = requestProcessorChain.getRequestPath();
        if (requestPath.startsWith(JSP_RESOURCE_PREFIX)) {
            requestDispatcher.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }
}
