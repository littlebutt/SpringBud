package org.springbud.mvc.render.impl;

import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.render.ResultRender;

import javax.servlet.http.HttpServletResponse;

public class InternalErrorResultRender implements ResultRender {
    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
