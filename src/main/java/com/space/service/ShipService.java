package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ShipService {

    List<Ship> getAllShips(Specification<Ship> specification);

    Page<Ship> getAllShips(Specification<Ship> specification, Pageable sortedPageable);

    Ship createShip(Ship requestShip);

    Ship getShipById(Long id);

    Ship updateShipById(Long id, Ship ship);

    void deleteShipById(Long id);

    Long getIdFromString(String id);

    Specification<Ship> filterByName(String name);

    Specification<Ship> filterByPlanet(String planet);

    Specification<Ship> filterByShipType(ShipType shipType);

    Specification<Ship> filterByProdDate(Long before, Long after);

    Specification<Ship> filterByUsage(Boolean isUsed);

    Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed);

    Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize);

    Specification<Ship> filterByRating(Double minRating, Double maxRating);

}
