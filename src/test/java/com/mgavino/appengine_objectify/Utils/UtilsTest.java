package com.mgavino.appengine_objectify.Utils;

import com.mgavino.appengine_objectify.entity.ExampleEntity;
import com.mgavino.appengine_objectify.utils.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@RunWith(SpringRunner.class)
public class UtilsTest {

    @Test
    public void merge() {

        ExampleEntity source = new ExampleEntity();
        ExampleEntity target = new ExampleEntity();

        source.setTitle("Title2");

        target.setTitle("Title");
        target.setDescription("Description");

        Utils.merge(source, target);

        Assert.assertEquals("Title2", target.getTitle());

    }

}
