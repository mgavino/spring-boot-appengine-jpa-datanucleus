package com.mgavino.restful_appengine_objectify.controller;

import com.mgavino.restful_appengine_objectify.entity.ExampleEntity;
import com.mgavino.restful_appengine_objectify.service.GenericService;
import com.mgavino.restful_appengine_objectify.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/example")
public class ExampleController {

    @Autowired
    private GenericService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ExampleEntity>> getAll() {

        List<ExampleEntity> entities = service.findAll(ExampleEntity.class);
        if (entities == null || entities.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<List<ExampleEntity>>(entities, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ExampleEntity> get(@PathVariable("id") long id) {

        ExampleEntity entity = service.find(id, ExampleEntity.class);
        if (entity == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ExampleEntity> post(@RequestBody ExampleEntity entity, UriComponentsBuilder ucBuilder) {

        if (entity.getId() != null) {
            entity.setId(null);
        }

        entity = service.save(entity);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/example/{id}").buildAndExpand(entity.getId()).toUri());
        return new ResponseEntity<ExampleEntity>(entity, headers, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ExampleEntity> put(@PathVariable("id") Long id, @RequestBody ExampleEntity entity) {

        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);
        if (foundEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        entity.setId(id);
        service.save(entity);

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ExampleEntity> patch(@PathVariable("id") Long id, @RequestBody ExampleEntity entity) {

        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);
        if (foundEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (entity.getId() != null) {
            entity.setId(null);
        }

        Utils.merge( entity, foundEntity );

        service.save(foundEntity);

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ExampleEntity> delete(@PathVariable("id") Long id) {

        ExampleEntity entity = service.find(id, ExampleEntity.class);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        service.delete(id, ExampleEntity.class);
        return new ResponseEntity<ExampleEntity>(HttpStatus.NO_CONTENT);
    }

}
