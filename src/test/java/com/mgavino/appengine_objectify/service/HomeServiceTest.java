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

    private static LocalDatastoreHelper localDatastore;

    /**
     * Init a local instance of datastore with the default parameters.
     *  HOST: localhost
     *  PORT: 8080
     *  PROJECT_ID: test-project- + UUID.randomUUID()
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        localDatastore = LocalDatastoreHelper.create(1.0); // 100% global consistency
        localDatastore.start();
    }

    /**
     * Stop the local instance of datastore
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (localDatastore != null) {
            try {
                localDatastore.stop();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void contextLoads() {
        Assert.assertNotNull(service);
    }

    @Test
    public void save() {

        Datastore ds = localDatastore.getOptions().getService();

        ObjectifyService.init(new ObjectifyFactory(ds));
        ObjectifyService.register(HomeEntity.class);

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