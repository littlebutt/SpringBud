package org.springbud.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ContainerBeanTests {

    private static ContainerBean containerBean;

    @BeforeAll
    static void setUp() {
        containerBean = ContainerBean.getInstance();
    }

    @Test
    public void testSize() {
        Assertions.assertEquals(0, containerBean.size());
    }

    @Test
    public void testLoaded() throws ClassNotFoundException, IOException {
        Assertions.assertFalse(ContainerBean.isLoaded());
        containerBean.loadBeans("com.littlebutt");
        Assertions.assertTrue(ContainerBean.isLoaded());
    }
}
