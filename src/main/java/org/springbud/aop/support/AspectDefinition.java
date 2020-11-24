package org.springbud.aop.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springbud.aop.PointcutLocator;

@AllArgsConstructor
@Data
public class AspectDefinition {
    private int orderIndex;
    private DefaultAspect defaultAspect;
    private PointcutLocator pointcutLocator;
}
