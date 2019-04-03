package com.mgavino.appengine_objectify.service;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.mgavino.appengine_objectify.entity.HomeEntity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HomeServiceTest {

    @Autowired
    private GenericService service;

    @Test
    public void contextLoads() {
        Assert.assertNotNull(service);
    }

    @Test
    public void save() {

        HomeEntity entity = new HomeEntity();
        entity.setTitle("Title");
        entity.setDescription("Description");

        entity = service.save(entity);

        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.getId());
        Assert.assertNotEquals(0, entity.getId().longValue());

    }

    @Test
    public void update() {

    }

    @Test
    public void get() {

    }

    @Test
    public void delete() {

    }

}