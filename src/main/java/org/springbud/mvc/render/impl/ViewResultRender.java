package org.springbud.mvc.render.impl;

import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.render.ResultRender;
import org.springbud.mvc.type.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ViewResultRender implements ResultRender {

    private ModelAndView modelAndView;

    public ViewResultRender(Object result) {
        if (result instanceof ModelAndView) {
            this.modelAndView = (ModelAndView)result;
        } else if (result instanceof String) {
            this.modelAndView = new ModelAndView().setView((String) result);
        } else {
            throw new RuntimeException("Illegal Request Resqult Type");
        }
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        HttpServletRequest request = requestProcessorChain.getRequest();
        HttpServletResponse response = requestProcessorChain.getResponse();
        String path = modelAndView.getView();
        Map<String, Object> model = modelAndView.getModel();
        model.forEach(request::setAttribute);
        request.getRequestDispatcher("/templates/" + path).forward(request, response);
    }
}
