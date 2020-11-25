package org.springbud.mvc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControllerMethod {

    private Class<?> controllerClass;

    private Method invokeMethod;

    private Map<String, Class<?>> methodParams;

}
