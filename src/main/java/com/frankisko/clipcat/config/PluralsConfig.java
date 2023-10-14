package com.frankisko.clipcat.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PluralsConfig {
    public String getPluralFor(String singular) {
        Map<String, String> plurals = new HashMap<>();
        plurals.put("group", "groups");
        plurals.put("tag", "tags");

        return plurals.get(singular);
    }
}
