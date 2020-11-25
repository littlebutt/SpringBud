package org.springbud.mvc.processor.impl;

import org.springbud.mvc.RequestProcessorChain;
import org.springbud.mvc.processor.RequestProcessor;

public class PreRequestProcessor implements RequestProcessor {
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getRequest().setCharacterEncoding("UTF-8");
        String requestPath = requestProcessorChain.getRequestPath();
        if (requestPath.length()> 1 && requestPath.endsWith("/")) {
            requestProcessorChain.setRequestPath(requestPath
                    .substring(0, requestPath.length() - 1));
        }
        return true;
    }
}
