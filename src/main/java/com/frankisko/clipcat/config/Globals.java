package com.frankisko.clipcat.config;

import org.springframework.stereotype.Component;

@Component
public class Globals {

    private Integer idCollection;

    public Integer getIdCollection() {
        return idCollection;
    }

    public void setIdCollection(Integer idCollection) {
        this.idCollection = idCollection;
    }
}