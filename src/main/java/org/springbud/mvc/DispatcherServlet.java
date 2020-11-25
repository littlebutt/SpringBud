package org.springbud.mvc;

import org.springbud.aop.AspectWeaver;
import org.springbud.core.ContainerBean;
import org.springbud.inject.DependencyInjector;
import org.springbud.mvc.processor.RequestProcessor;
import org.springbud.mvc.processor.impl.ControllerRequestProcessor;
import org.springbud.mvc.processor.impl.JspResourceRequestProcessor;
import org.springbud.mvc.processor.impl.PreRequestProcessor;
import org.springbud.mvc.processor.impl.StaticResourceRequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DispatcherServlet extends HttpServlet {

    List<RequestProcessor> requestProcessors = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        ContainerBean containerBean = ContainerBean.getInstance();
        containerBean.loadBeans("");
        new AspectWeaver().doAop();
        new DependencyInjector().doIoC();

        requestProcessors.add(new PreRequestProcessor());
        requestProcessors.add(new StaticResourceRequestProcessor(getServletContext()));
        requestProcessors.add(new JspResourceRequestProcessor(getServletContext()));
        requestProcessors.add(new ControllerRequestProcessor());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestProcessorChain requestProcessorChain = new RequestProcessorChain(requestProcessors.iterator(), req, resp);
        requestProcessorChain.doRequestProcessorChain();
        requestProcessorChain.doRender();
    }
}
