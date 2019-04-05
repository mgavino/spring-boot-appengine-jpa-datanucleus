package com.mgavino.restful_appengine_objectify.entity;

import com.googlecode.objectify.annotation.Id;

public abstract class IdentifyEntity {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
