package com.example.pawcare.controller;

import com.example.pawcare.model.Breed;
import com.example.pawcare.model.Specie;
import com.example.pawcare.repository.BreedRepository;
import com.example.pawcare.repository.SpecieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
public class LookupController {

    @Autowired private SpecieRepository specieRepository;
    @Autowired private BreedRepository breedRepository;

    @GetMapping("/species")
    public List<Specie> getSpecies() {
        return specieRepository.findAll();
    }

    @GetMapping("/breeds")
    public List<Breed> getBreeds() {
        return breedRepository.findAll();
    }

    @GetMapping("/breeds/specie/{specieId}")
    public List<Breed> getBreedsBySpecie(@PathVariable Integer specieId) {
        return breedRepository.findBySpecieSpecieId(specieId);
    }
}
