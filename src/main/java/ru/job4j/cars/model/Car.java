package ru.job4j.cars.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "engine_id")
    private Engine engine;
    @OneToMany(mappedBy = "car")
    private Set<HistoryOwner> historyOwners = new HashSet<>();
}
