package org.springbud.mvc.processor;

import org.springbud.mvc.RequestProcessorChain;

public interface RequestProcessor {
    boolean process(RequestProcessorChain requestProcessorChain) throws Exception;
}
