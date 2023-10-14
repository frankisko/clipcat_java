package com.frankisko.clipcat.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.frankisko.clipcat.dto.LocationRecord;
import com.frankisko.clipcat.services.LocationService;

@Controller
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/locations/{idLocation}/open")
    @ResponseBody
    public void open(@PathVariable(name = "idLocation") Integer idLocation) {
        LocationRecord record = locationService.getById(idLocation);

        locationService.open(record.getLocation());
    }

}