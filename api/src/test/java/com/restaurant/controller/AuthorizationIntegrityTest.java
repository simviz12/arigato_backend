package com.restaurant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AuthorizationIntegrityTest {

    private static final List<String> PUBLIC_WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/health"
    );

    @Test
    public void testAllEndpointsAreSecured() throws Exception {
        List<Class<?>> controllers = findRestControllers("com.restaurant");
        List<String> unsecuredEndpoints = new ArrayList<>();

        for (Class<?> controller : controllers) {
            PreAuthorize classPreAuthorize = controller.getAnnotation(PreAuthorize.class);
            RequestMapping classRequestMapping = controller.getAnnotation(RequestMapping.class);
            String basePath = classRequestMapping != null && classRequestMapping.value().length > 0 ? classRequestMapping.value()[0] : "";

            for (Method method : controller.getDeclaredMethods()) {
                if (isEndpointMethod(method)) {
                    PreAuthorize methodPreAuthorize = method.getAnnotation(PreAuthorize.class);
                    String endpointPath = basePath + getMethodPath(method);
                    
                    boolean isSecured = (classPreAuthorize != null || methodPreAuthorize != null);
                    boolean isPublic = PUBLIC_WHITELIST.contains(endpointPath);

                    if (!isSecured && !isPublic) {
                        unsecuredEndpoints.add(controller.getSimpleName() + "." + method.getName() + " (" + endpointPath + ")");
                    }
                }
            }
        }

        if (!unsecuredEndpoints.isEmpty()) {
            fail("Found unsecured endpoints without @PreAuthorize:\n" + String.join("\n", unsecuredEndpoints));
        } else {
            assertTrue(true);
        }
    }

    private boolean isEndpointMethod(Method method) {
        return method.isAnnotationPresent(GetMapping.class) ||
               method.isAnnotationPresent(PostMapping.class) ||
               method.isAnnotationPresent(PutMapping.class) ||
               method.isAnnotationPresent(DeleteMapping.class) ||
               method.isAnnotationPresent(RequestMapping.class);
    }

    private String getMethodPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            String[] val = method.getAnnotation(GetMapping.class).value();
            return val.length > 0 ? val[0] : "";
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            String[] val = method.getAnnotation(PostMapping.class).value();
            return val.length > 0 ? val[0] : "";
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            String[] val = method.getAnnotation(PutMapping.class).value();
            return val.length > 0 ? val[0] : "";
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            String[] val = method.getAnnotation(DeleteMapping.class).value();
            return val.length > 0 ? val[0] : "";
        }
        if (method.isAnnotationPresent(RequestMapping.class)) {
            String[] val = method.getAnnotation(RequestMapping.class).value();
            return val.length > 0 ? val[0] : "";
        }
        return "";
    }

    private List<Class<?>> findRestControllers(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();
        String path = packageName.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null) {
            File directory = new File(resource.getFile());
            findClasses(directory, packageName, controllers);
        }
        return controllers;
    }

    private void findClasses(File directory, String packageName, List<Class<?>> classes) throws Exception {
        if (!directory.exists()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                findClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(RestController.class)) {
                    classes.add(clazz);
                }
            }
        }
    }
}
