package com.mgavino.restful_appengine_objectify.service;

import com.googlecode.objectify.ObjectifyService;
import com.mgavino.restful_appengine_objectify.entity.IdentifyEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenericService {

    public <T extends IdentifyEntity> List<T> findAll(Class<T> classType) {
        return ObjectifyService.ofy().load().type(classType).list();
    }

    public <T extends IdentifyEntity> T find(Long id, Class<T> classType) {
        return ObjectifyService.ofy().load().type(classType).id(id).now();
    }

    public <T extends IdentifyEntity> T save(T entity) {
        ObjectifyService.ofy().save().entity(entity).now();
        return entity;
    }

    public <T extends IdentifyEntity> void delete(Long id, Class<T> classType) {
        ObjectifyService.ofy().delete().type(classType).id(id).now();
    }

}
