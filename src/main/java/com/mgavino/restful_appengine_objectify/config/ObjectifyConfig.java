package com.mgavino.restful_appengine_objectify.config;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Set;

@Configuration
public class ObjectifyConfig {

    @Value("${objectify.server}")
    private String objectifyServer;

    private LocalDatastoreHelper localDatastore;

    @PostConstruct
    private void initServer() throws Exception {

        switch (objectifyServer) {

            case "appengine":

                // init objectify service with appengine datastore server
                ObjectifyService.init();
                break;

            case "localhost":

                // start an emulator datastore server
                localDatastore = LocalDatastoreHelper.create(1.0);
                localDatastore.start();

                // init objectify service
                Datastore ds = localDatastore.getOptions().getService();
                ObjectifyService.init(new ObjectifyFactory(ds));

            default: // localhost case

        }

        // register entities
        registerEntities();

    }

    @PreDestroy
    private void stopServer() {

        if ("localhost".equals(objectifyServer)) {
            if (localDatastore != null) {
                try {
                    localDatastore.stop();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void registerEntities() {

        // register entities
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        Set<BeanDefinition> entityBeanDefinitions = scanner.findCandidateComponents("com.mgavino.restful_appengine_objectify");

        entityBeanDefinitions.stream()
                .map( bean -> {
                    try {
                        return Class.forName(bean.getBeanClassName());
                    } catch (Exception exception) {
                        return null;
                    }
                } )
                .filter( entityClass -> entityClass != null )
                .forEach( entityClass -> ObjectifyService.register(entityClass) );

    }

}
