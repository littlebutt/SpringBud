package org.springbud.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class ClassUtilTests {

    @Test
    public void testExtractPackageClasses() throws ClassNotFoundException, IOException {
        Set<Class<?>> testSet = null;

        testSet = ClassUtil.extractPackageClasses("org.springbud.core.annotation");

        assert testSet != null;
        Assertions.assertEquals(4, testSet.size());
    }

    @Test
    public void testExtractPackageClassesByJar()throws ClassNotFoundException, IOException {
//        Set<Class<?>> testSet = null;
//
//        testSet = ClassUtil.extractPackageClasses("C:\\Users\\luoga\\Desktop\\bot\\springbutt\\src\\main\\resources\\Component.jar");
//
//        assert testSet != null;
    }
}
