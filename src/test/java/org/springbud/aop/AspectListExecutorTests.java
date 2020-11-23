package org.springbud.aop;

import org.junit.jupiter.api.Test;
import org.springbud.aop.mock.MockAspect;
import org.springbud.aop.support.AspectDefinition;

import java.util.ArrayList;
import java.util.List;

public class AspectListExecutorTests {

    @Test
    public void testAspectListExecutor() {
        List<AspectDefinition> aspectDefinitions = new ArrayList<>();
        aspectDefinitions.add(new AspectDefinition(2, new MockAspect()));
        aspectDefinitions.add(new AspectDefinition(3, new MockAspect()));
        aspectDefinitions.add(new AspectDefinition(4, new MockAspect()));
        aspectDefinitions.add(new AspectDefinition(5, new MockAspect()));
        aspectDefinitions.add(new AspectDefinition(1, new MockAspect()));
        AspectListExecutor aspectListExecutor = new AspectListExecutor(AspectListExecutorTests.class, aspectDefinitions);
        List<AspectDefinition> sortedAspectDefinitions = aspectListExecutor.getAspectDefinitionList();
        for (AspectDefinition aspectDefinition : sortedAspectDefinitions) {
            System.out.println(aspectDefinition.getOrderIndex());
        }
    }
}
