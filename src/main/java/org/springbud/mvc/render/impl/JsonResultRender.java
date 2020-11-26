package org.springbud.mvc.render.impl;

import com.alibaba.fastjson.JSON;
import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.render.ResultRender;

import java.io.PrintWriter;

public class JsonResultRender implements ResultRender {
    private Object result;
    public JsonResultRender(Object result) {
        this.result = result;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getResponse().setContentType("application/json");
        requestProcessorChain.getResponse().setCharacterEncoding("UTF-8");
        try(PrintWriter writer = requestProcessorChain.getResponse().getWriter()) {
            writer.write(JSON.toJSONString(result));
            writer.flush();
        }
    }
}
