package ru.job4j.cars.repository;

import ru.job4j.cars.model.Participate;

import java.util.Collection;
import java.util.Optional;

public interface ParticipateRepository {
    Participate create(Participate participate);

    Collection<Participate> findAll();

    Optional<Participate> findById(int id);

    void delete(int id);
}
