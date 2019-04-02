package com.mgavino.appengine_objectify.config;

import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class ObjectifyConfig {

    @Around("anyMethod() && serviceClasses()")
    public Object transaction(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result;
        try (Closeable closeable = ObjectifyService.begin()) {
            result = joinPoint.proceed();
        }
        return result;

    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceClasses() {}

    @Pointcut("execution(* *(..))")
    public void anyMethod() {}

}
