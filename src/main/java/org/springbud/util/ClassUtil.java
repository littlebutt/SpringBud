package org.springbud.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class ClassUtil {

    private static final String FILE_PROTOCOL = "file";

    public static Set<Class<?>> extractPackageClasses(String className) throws ClassNotFoundException, IOException {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        if (className.endsWith(".jar")) {
            URL url = new URL("file:" + className);
            ClassLoader classLoader = new URLClassLoader(new URL[]{url});
            JarFile jarFile = new JarFile(new File(className));
            Enumeration<JarEntry> es = jarFile.entries();
            while (es.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) es.nextElement();
                String name = jarEntry.getName();
                Class<?> clazz = classLoader.loadClass(name.replace(File.separator, ".").substring(0,name.length() - 6));
                classSet.add(clazz);
            }
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String replacedClassName = className.replace('.', '/');
            URL url = classLoader.getResource(replacedClassName);
            if (url == null) {
                log.warn("Cannot find any resource in " + className);
                return null;
            }
            File classDirectory = new File(url.getPath());
            extractClassFiles(classSet, classDirectory, className);
        }
        return classSet;
    }

    private static void extractClassFiles(Set<Class<?>> classSet, File classDirectory, String className) throws ClassNotFoundException {
        if (!classDirectory.isDirectory()) {
            return;
        }
        File[] fileDirectories = classDirectory.listFiles(new FileFilter() {
            @SneakyThrows
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }else if (pathname.getAbsolutePath().endsWith(".class")){
                    addToClassSet(pathname.getAbsolutePath());
                }
                return false;
            }

            @SneakyThrows
            private void addToClassSet(String absoluteClassPath) {
                absoluteClassPath = absoluteClassPath.replace(File.separator, ".");
                String pathName = absoluteClassPath.substring(absoluteClassPath.indexOf(className), absoluteClassPath.length() - 6);
                //System.out.println(pathName);
                Class target = Class.forName(pathName);
                classSet.add(target);
            }
        });
        if (fileDirectories != null)
            for (File f : fileDirectories) {
                extractClassFiles(classSet, f, className);
            }
    }

    public static <T> T getInstance(Class<T> clazz, boolean accessable) {
        try {
            Constructor constructor = clazz.getConstructor();
            constructor.setAccessible(accessable);
            T t = (T)constructor.newInstance();
            return t;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void setField(Field field, Object target, Object value, boolean accessible) {
        field.setAccessible(accessible);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.warn("Failed to set field");
            e.printStackTrace();
        }
    }
}
