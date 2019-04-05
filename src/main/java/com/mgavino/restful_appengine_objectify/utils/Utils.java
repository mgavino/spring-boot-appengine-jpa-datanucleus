package com.mgavino.restful_appengine_objectify.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Utils {

    public static <T> void merge(T source, T target) {

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(source.getClass());
        Arrays.stream(methods)
                .filter( method -> method.getName().startsWith("get") )
                .filter( method -> ReflectionUtils.invokeMethod(method, source) != null )
                .filter( method -> ReflectionUtils.findMethod(target.getClass(),
                            method.getName().replaceFirst("get", "set"),
                            method.getReturnType() ) != null )
                .forEach( method -> ReflectionUtils.invokeMethod(
                        ReflectionUtils.findMethod(target.getClass(),
                                method.getName().replaceFirst("get", "set"),
                                method.getReturnType()
                        ),
                        target,
                        ReflectionUtils.invokeMethod(method, source) )
                );

    }

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

}
