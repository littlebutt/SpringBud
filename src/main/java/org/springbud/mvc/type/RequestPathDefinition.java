package org.springbud.mvc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPathDefinition {

    private String requestPath;

    private String requestMethod;
}
