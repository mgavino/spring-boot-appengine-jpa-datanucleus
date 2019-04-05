package com.mgavino.restful_appengine_objectify.entity;

import com.googlecode.objectify.annotation.Entity;

@Entity
public class ExampleEntity extends IdentifyEntity {

    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
