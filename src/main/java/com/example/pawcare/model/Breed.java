package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Breed")
public class Breed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Breed_ID")
    private Integer breedId;

    @Column(name = "Breed_name", nullable = false, length = 50)
    private String breedName;

    @ManyToOne
    @JoinColumn(name = "Specie_ID", nullable = false)
    private Specie specie;

    public Breed() {}

    public Integer getBreedId() { return breedId; }
    public void setBreedId(Integer breedId) { this.breedId = breedId; }

    public String getBreedName() { return breedName; }
    public void setBreedName(String breedName) { this.breedName = breedName; }

    public Specie getSpecie() { return specie; }
    public void setSpecie(Specie specie) { this.specie = specie; }
}
