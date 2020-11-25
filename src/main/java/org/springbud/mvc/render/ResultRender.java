package org.springbud.mvc.render;

import org.springbud.mvc.RequestProcessorChain;

public interface ResultRender {
    void render(RequestProcessorChain requestProcessorChain) throws Exception;
}
