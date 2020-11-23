package org.springbud.aop.support;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AspectDefinition {
    private int orderIndex;
    private DefaultAspect defaultAspect;
}
