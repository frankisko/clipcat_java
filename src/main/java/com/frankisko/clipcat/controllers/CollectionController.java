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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dto.CollectionRow;
import com.frankisko.clipcat.dto.CollectionProcess;
import com.frankisko.clipcat.dto.CollectionRecord;
import com.frankisko.clipcat.services.ApiClientService;
import com.frankisko.clipcat.services.CollectionService;
import com.frankisko.clipcat.services.MediaService;

@Controller
public class CollectionController {

    private CollectionService collectionService;

    private MediaService mediaService;

    private ApiClientService apiClientService;

    private Globals globals;

    public CollectionController(CollectionService collectionService, MediaService mediaService,
                                ApiClientService apiClientService, Globals globals) {
        this.collectionService = collectionService;
        this.mediaService = mediaService;
        this.apiClientService = apiClientService;
        this.globals = globals;
    }

    @GetMapping(path = {"/", "/collections/index"})
    public ModelAndView index() {
        List<CollectionRow> rows = apiClientService.collectionsAll();

        ModelAndView modelAndView = new ModelAndView("collections/index");
        modelAndView.addObject("rows", rows);

        return modelAndView;
    }

    @GetMapping("/collections/create")
    public ModelAndView create() {
        CollectionRecord collectionRecord = new CollectionRecord();

        ModelAndView modelAndView = new ModelAndView("collections/create");
        modelAndView.addObject("collectionRecord", collectionRecord);

        return modelAndView;
    }

    @PostMapping("/collections/store")
    public ModelAndView store(@Validated CollectionRecord collectionRecord, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("collections/create");
            modelAndView.addObject("collectionRecord", collectionRecord);

            return modelAndView;
        }

        apiClientService.collectionInsert(collectionRecord);

        ModelAndView modelAndView = new ModelAndView("redirect:/collections/index");
        return modelAndView;
    }

    @GetMapping("/collections/{idCollection}/edit")
    public ModelAndView edit(@PathVariable(name = "idCollection") Integer idCollection) {
        CollectionRecord collectionRecord = apiClientService.collectionById(idCollection);

        ModelAndView modelAndView = new ModelAndView("collections/edit");
        modelAndView.addObject("idCollection", idCollection);
        modelAndView.addObject("collectionRecord", collectionRecord);

        return modelAndView;
    }

    @PostMapping("/collections/{idCollection}/update")
    public ModelAndView update(@PathVariable(name = "idCollection") Integer idCollection, @Validated CollectionRecord collectionRecord, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("collections/edit");
            modelAndView.addObject("idCollection", idCollection);
            modelAndView.addObject("collectionRecord", collectionRecord);

            return modelAndView;
        }

        apiClientService.collectionUpdate(idCollection, collectionRecord);

        ModelAndView modelAndView = new ModelAndView("redirect:/collections/index");
        return modelAndView;
    }

    @GetMapping("/collections/{idCollection}/destroy")
    @ResponseBody
    public ModelAndView destroy(@PathVariable(name = "idCollection") Integer idCollection) {
        apiClientService.collectionDelete(idCollection);

        ModelAndView modelAndView = new ModelAndView("redirect:/collections/index");
        return modelAndView;
    }

    @GetMapping("/collections/{idCollection}/select")
    public ModelAndView select(@PathVariable(name = "idCollection") Integer idCollection) {
        globals.setIdCollection(idCollection);

        ModelAndView modelAndView = new ModelAndView("redirect:/media/index");

        return modelAndView;
    }

    @GetMapping("/collections/{idCollection}/scrap")
    public ModelAndView scrap(@PathVariable(name = "idCollection") Integer idCollection) {
        collectionService.scrap(idCollection);

        ModelAndView modelAndView = new ModelAndView("collections/scrap");
        return modelAndView;
    }


    @GetMapping("/collections/{idCollection}/process")
    @ResponseBody
    public CollectionProcess process(@PathVariable(name = "idCollection") Integer idCollection) {
        Integer mediaInCollection = mediaService.countMediaInCollection(idCollection);
        Integer pendingMediaInCollection = mediaService.countPendingMediaInCollection(idCollection);

        Integer scrappedMedia = mediaInCollection - pendingMediaInCollection;
        Float percentage = ((float) scrappedMedia * 100) / mediaInCollection;
        String progress = String.format("%.2f", percentage);

        //process media
        String name = mediaService.processMedia(idCollection);

        CollectionProcess response = new CollectionProcess();
        response.setIdCollection(idCollection);
        response.setName(name);
        response.setProgress(Float.parseFloat(progress));
        response.setLeft(pendingMediaInCollection);

        return response;
    }

    //api
    @GetMapping(path = {"/api/collections"})
    @ResponseBody
    public List<CollectionRow> apiCollectionsAll() {
        return collectionService.getAll();
    }

    @GetMapping(path = {"/api/collections/{idCollection}"})
    @ResponseBody
    public CollectionRecord apiCollectionById(@PathVariable(name = "idCollection") Integer idCollection) {
        return collectionService.getById(idCollection);
    }

    @PostMapping(path = {"/api/collections"})
    @ResponseBody
    public void apiCollectionInsert(@RequestBody CollectionRecord record) {
        //TODO validations (move them from store function)
        collectionService.insert(record);
    }

    @PutMapping(path = {"/api/collections/{idCollection}"})
    @ResponseBody
    public void apiCollectionUpdate(@PathVariable(name = "idCollection") Integer idCollection, @RequestBody CollectionRecord record) {
        //TODO validations (move them from store function)
        collectionService.update(idCollection, record);
    }

    @DeleteMapping(path = {"/api/collections/{idCollection}"})
    @ResponseBody
    public void apiCollectionDelete(@PathVariable(name = "idCollection") Integer idCollection) {
        collectionService.delete(idCollection);
    }

}
