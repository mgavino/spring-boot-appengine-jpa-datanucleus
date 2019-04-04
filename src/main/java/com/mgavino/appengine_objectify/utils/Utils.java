package com.mgavino.appengine_objectify.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Utils {

    public static <T> void merge(T source, T target) {

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(source.getClass());
        Arrays.stream(methods)
                .filter( method -> method.getName().startsWith("get") )
                .filter( method -> ReflectionUtils.invokeMethod(method, source) != null )
                .forEach( method -> ReflectionUtils.invokeMethod(
                        ReflectionUtils.findMethod(target.getClass(), method.getName().replaceFirst("get", "set") ),
                        target,
                        ReflectionUtils.invokeMethod(method, source) )
                );

    }

}
