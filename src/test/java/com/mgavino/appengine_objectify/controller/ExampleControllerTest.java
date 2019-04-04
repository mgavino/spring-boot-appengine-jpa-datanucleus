package com.mgavino.appengine_objectify.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgavino.appengine_objectify.entity.ExampleEntity;
import com.mgavino.appengine_objectify.service.GenericService;
import com.mgavino.appengine_objectify.utils.Utils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExampleControllerTest {

    @Autowired
    private ExampleController controller;

    @Autowired
    private GenericService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void contextLoads() {
        Assert.assertNotNull(controller);
    }

    @Test
    public void post() throws Exception {

        ExampleEntity entity = new ExampleEntity();
        entity.setTitle("Title");
        entity.setDescription("Description");

        String location = mockMvc.perform(
            MockMvcRequestBuilders.post("/example")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entity)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.header().exists("location"))
            .andReturn().getResponse().getHeader("location");

        String id = location.substring( location.lastIndexOf("/") + 1 );
        Assert.assertTrue(Utils.isNumeric(id));

        ExampleEntity savedEntity = service.find(Long.parseLong(id), ExampleEntity.class);
        Assert.assertNotNull(savedEntity);
        Assert.assertEquals("Title", savedEntity.getTitle());
        Assert.assertEquals("Description", savedEntity.getDescription());

    }

    @Test
    public void put() throws Exception {

        ExampleEntity entity = saveHelper();
        Long id = entity.getId();

        entity.setId(null);
        entity.setTitle("Title2");

        mockMvc.perform(
            MockMvcRequestBuilders.put("/example/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entity)))
            .andExpect(MockMvcResultMatchers.status().isOk());

        ExampleEntity updatedEntity = service.find(id, ExampleEntity.class);
        Assert.assertNotNull(updatedEntity);
        Assert.assertEquals("Description", updatedEntity.getDescription());
        Assert.assertEquals("Title2", updatedEntity.getTitle());

    }

    @Test
    public void patch() throws Exception {

        ExampleEntity entity = saveHelper();
        Long id = entity.getId();

        ExampleEntity patchEntity = new ExampleEntity();
        patchEntity.setTitle("Title2");

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/example/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchEntity)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        ExampleEntity updatedEntity = service.find(id, ExampleEntity.class);
        Assert.assertNotNull(updatedEntity);
        Assert.assertEquals("Description", updatedEntity.getDescription());
        Assert.assertEquals("Title2", updatedEntity.getTitle());

    }

    @Test
    public void delete() throws Exception {

        ExampleEntity entity = saveHelper();
        Long id = entity.getId();

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/example/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        ExampleEntity deleteEntity = service.find(id, ExampleEntity.class);
        Assert.assertNull(deleteEntity);

    }

    @Test
    public void get() throws Exception {

        ExampleEntity entity = saveHelper();
        Long id = entity.getId();

        String contentResponse = mockMvc.perform(
                                    MockMvcRequestBuilders.get("/example/" + id)
                                            .contentType(MediaType.APPLICATION_JSON))
                                    .andExpect(MockMvcResultMatchers.status().isOk())
                                    .andExpect(MockMvcResultMatchers.content().string( Matchers.notNullValue() ))
                                    .andReturn().getResponse().getContentAsString();

        ExampleEntity entityResponse = objectMapper.readValue(contentResponse, ExampleEntity.class);
        Assert.assertNotNull(entityResponse);
        Assert.assertEquals(id, entityResponse.getId());
        Assert.assertEquals(entity.getTitle(), entityResponse.getTitle());
        Assert.assertEquals(entity.getDescription(), entityResponse.getDescription());

    }

    @Test
    public void getAll() throws Exception {

        saveHelper();
        saveHelper();
        saveHelper();

        String contentResponse = mockMvc.perform(
                MockMvcRequestBuilders.get("/example")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string( Matchers.notNullValue() ))
                .andReturn().getResponse().getContentAsString();

        List<ExampleEntity> entitiesResponse =
                objectMapper.readValue(contentResponse, new TypeReference<List<ExampleEntity>>(){});

        Assert.assertNotNull(entitiesResponse);
        Assert.assertEquals(3, entitiesResponse.size());

    }

    private ExampleEntity saveHelper() {

        ExampleEntity entity = new ExampleEntity();
        entity.setTitle("Title");
        entity.setDescription("Description");

        return service.save(entity);

    }

}
