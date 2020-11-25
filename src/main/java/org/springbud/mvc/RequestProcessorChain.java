package org.springbud.mvc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springbud.mvc.processor.RequestProcessor;
import org.springbud.mvc.render.ResultRender;
import org.springbud.mvc.render.impl.DefaultResultRender;
import org.springbud.mvc.render.impl.InternalErrorResultRender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

@Data
@Slf4j
public class RequestProcessorChain {

    private Iterator<RequestProcessor> requestProcessorIterator;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private String requestPath;

    private String requestMethod;

    private int responseCode;

    private ResultRender resultRender;

    public RequestProcessorChain(Iterator<RequestProcessor> iterator, HttpServletRequest req, HttpServletResponse resp) {
        this.requestProcessorIterator = iterator;
        this.request = req;
        this.response = resp;
        this.requestPath = req.getRequestURI();
        this.requestMethod = req.getMethod();
        this.responseCode = HttpServletResponse.SC_CONTINUE;
    }

    /**
     * Use job chain to do the request processing and the order of job is stored in the requestProcessorIterator
     */
    public void doRequestProcessorChain() {
        try {
            while (this.requestProcessorIterator.hasNext()) {
                if (!this.requestProcessorIterator.next().process(this)){
                    break;
                }
            }
        }catch (Exception e){
            this.resultRender = new InternalErrorResultRender();
            log.error("RequestProcessorChain Error", e);
        }
    }

    /**
     * Render the result with given resultRender
     */
    public void doRender() {
        if (this.resultRender == null){
            this.resultRender = new DefaultResultRender();
        }
        try {
            this.resultRender.render(this);
        } catch (Exception e) {
            log.error("Render Error", e);
            e.printStackTrace();
        }
    }
}
