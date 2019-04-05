package com.mgavino.restful_appengine_objectify.service;

import com.mgavino.restful_appengine_objectify.entity.ExampleEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleServiceTest {

    @Autowired
    private GenericService service;

    @Test
    public void contextLoads() {
        Assert.assertNotNull(service);
    }

    @Test
    public void save() {

        ExampleEntity entity = saveHelper();

        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.getId());
        Assert.assertNotEquals(0, entity.getId().longValue());

    }

    @Test
    public void update() {

        ExampleEntity entity = saveHelper();

        Long id = entity.getId();
        String title = entity.getTitle();
        String descr = entity.getDescription();

        entity.setDescription("Description Updated");

        entity = service.save(entity);

        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.getId());
        Assert.assertEquals(id, entity.getId());
        Assert.assertEquals(title, entity.getTitle());
        Assert.assertNotEquals(descr, entity.getDescription());
        Assert.assertEquals("Description Updated", entity.getDescription());

    }

    @Test
    public void get() {

        ExampleEntity entity = saveHelper();

        Long id = entity.getId();
        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);

        Assert.assertNotNull(foundEntity);
        Assert.assertNotNull(foundEntity.getId());
        Assert.assertEquals(entity.getId(), foundEntity.getId());
        Assert.assertEquals(entity.getTitle(), foundEntity.getTitle());
        Assert.assertEquals(entity.getDescription(), foundEntity.getDescription());

    }

    @Test
    public void getAll() {

        ExampleEntity entity = saveHelper();

        ExampleEntity entity2 = new ExampleEntity();
        entity2.setTitle("Title2");
        entity2.setDescription("Description2");
        service.save(entity2);

        List<ExampleEntity> entities = service.findAll(ExampleEntity.class);

        Assert.assertNotNull(entities);
        Assert.assertEquals(2, entities.size());

    }

    @Test
    public void delete() {

        ExampleEntity entity = saveHelper();

        Long id = entity.getId();
        service.delete(id, ExampleEntity.class);

        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);

        Assert.assertNull(foundEntity);

    }

    private ExampleEntity saveHelper() {

        ExampleEntity entity = new ExampleEntity();
        entity.setTitle("Title");
        entity.setDescription("Description");

        return service.save(entity);

    }

}