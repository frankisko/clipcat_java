package com.frankisko.clipcat.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.config.PluralsConfig;
import com.frankisko.clipcat.dto.MetadataRecord;
import com.frankisko.clipcat.dto.MetadataRow;
import com.frankisko.clipcat.entities.Metadata;
import com.frankisko.clipcat.services.ApiClientService;
import com.frankisko.clipcat.services.MetadataService;

@Controller
public class MetadataController {

    private MetadataService metadataService;

    private final ApiClientService apiClientService;

    private final Globals globals;

    private final PluralsConfig pluralsConfig;

    public MetadataController(MetadataService metadataService, ApiClientService apiClientService, Globals globals, PluralsConfig pluralsConfig) {
        this.metadataService = metadataService;
        this.apiClientService = apiClientService;
        this.globals = globals;
        this.pluralsConfig = pluralsConfig;
    }

    @GetMapping("/metadata/index")
    public ModelAndView index(@RequestParam(name = "type", required = true) String type) {
        List<MetadataRow> rows = apiClientService.metadataAllByType(type);

        ModelAndView modelAndView = new ModelAndView("metadata/index");
        modelAndView.addObject("rows", rows);
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("type", type);
        modelAndView.addObject("plural", pluralsConfig.getPluralFor(type));
        modelAndView.addObject("activeMenu", "metadata");

        return modelAndView;
    }

    @GetMapping("/metadata/create")
    public ModelAndView create(@RequestParam(name = "type", required = true) String type) {
        Metadata metadataRecord = new Metadata();
        metadataRecord.setType(type);

        ModelAndView modelAndView = new ModelAndView("metadata/create");
        modelAndView.addObject("metadataRecord", metadataRecord);
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("type", type);
        modelAndView.addObject("plural", pluralsConfig.getPluralFor(type));
        modelAndView.addObject("activeMenu", "metadata");

        return modelAndView;
    }

    @PostMapping("/metadata/store")
    public ModelAndView store(@Validated MetadataRecord metadataRecord, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("metadata/create");
            modelAndView.addObject("metadataRecord", metadataRecord);
            modelAndView.addObject("idCollection", globals.getIdCollection());
            modelAndView.addObject("type", metadataRecord.getType());
            modelAndView.addObject("plural", pluralsConfig.getPluralFor(metadataRecord.getType()));
            modelAndView.addObject("activeMenu", "metadata");

            return modelAndView;
        }

        apiClientService.metadataInsert(metadataRecord);

        ModelAndView modelAndView = new ModelAndView("redirect:/metadata/index?type=" + metadataRecord.getType());
        return modelAndView;
    }


    @GetMapping("/metadata/{idMetadata}/edit")
    public ModelAndView edit(@PathVariable(name = "idMetadata") Integer idMetadata) {
       MetadataRecord metadataRecord = apiClientService.metadataById(idMetadata);

        ModelAndView modelAndView = new ModelAndView("metadata/edit");
        modelAndView.addObject("metadataRecord", metadataRecord);
        modelAndView.addObject("idMetadata", idMetadata);
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("type", metadataRecord.getType());
        modelAndView.addObject("plural", pluralsConfig.getPluralFor(metadataRecord.getType()));
        modelAndView.addObject("activeMenu", "metadata");

        return modelAndView;
    }

    @PostMapping("/metadata/{idMetadata}/update")
    public ModelAndView update(@PathVariable(name = "idMetadata") Integer idMetadata, @Validated MetadataRecord metadataRecord, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("metadata/edit");
            modelAndView.addObject("metadataRecord", metadataRecord);
            modelAndView.addObject("idMetadata", idMetadata);
            modelAndView.addObject("idCollection", globals.getIdCollection());
            modelAndView.addObject("type", metadataRecord.getType());
            modelAndView.addObject("plural", pluralsConfig.getPluralFor(metadataRecord.getType()));
            modelAndView.addObject("activeMenu", "metadata");

            return modelAndView;
        }

        apiClientService.metadataUpdate(idMetadata, metadataRecord);

        ModelAndView modelAndView = new ModelAndView("redirect:/metadata/index?type=" + metadataRecord.getType());
        return modelAndView;
    }

    @GetMapping(path = {"/", "/metadata/{idMetadata}/destroy"})
    @ResponseBody
    public ModelAndView destroy(@PathVariable(name = "idMetadata") Integer idMetadata) {
        MetadataRecord record = apiClientService.metadataById(idMetadata);
        apiClientService.metadataDelete(idMetadata);

        ModelAndView modelAndView = new ModelAndView("redirect:/metadata/index?type=" + record.getType());
        return modelAndView;
    }

    //api
    @GetMapping(path = {"/api/metadata"})
    @ResponseBody
    public List<MetadataRow> apiMetadataAllByType(@RequestParam(name = "type", required = true) String type) {
        return metadataService.getAllByType(type);
    }

    @GetMapping(path = {"/api/metadata/{idMetadata}"})
    @ResponseBody
    public MetadataRecord apiMetadataById(@PathVariable(name = "idMetadata") Integer idMetadata) {
        return metadataService.getById(idMetadata);
    }

    @PostMapping(path = {"/api/metadata"})
    @ResponseBody
    public void apiMetadataInsert(@RequestBody MetadataRecord record) {
        //TODO validations (move them from store function)
        metadataService.insert(record);
    }

    @PutMapping(path = {"/api/metadata/{idMetadata}"})
    @ResponseBody
    public void apiMetadataUpdate(@PathVariable(name = "idMetadata") Integer idMetadata, @RequestBody MetadataRecord record) {
        //TODO validations (move them from store function)
        metadataService.update(idMetadata, record);
    }

    @DeleteMapping(path = {"/api/metadata/{idMetadata}"})
    @ResponseBody
    public void apiMetadataDelete(@PathVariable(name = "idMetadata") Integer idMetadata) {
        metadataService.delete(idMetadata);
    }

}
