package ru.job4j.cars.repository;

import ru.job4j.cars.model.HistoryOwner;

import java.util.Collection;
import java.util.Optional;

public interface HistoryOwnerRepository {
    HistoryOwner create(HistoryOwner historyOwner);

    Collection<HistoryOwner> findAll();

    Optional<HistoryOwner> findById(int id);

    void update(HistoryOwner historyOwner);

    void delete(int id);
}
