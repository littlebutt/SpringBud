package org.springbud.mvc.type;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    @Getter
    private String view;

    @Getter
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView setView(String view) {
        this.view = view;
        return this;
    }

    public ModelAndView setModel(String attribute, Object value) {
        this.model.put(attribute, value);
        return this;
    }
}
