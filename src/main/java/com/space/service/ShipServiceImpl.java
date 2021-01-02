package com.space.service;

import com.space.BadRequestException;
import com.space.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    @Autowired
    public void setShipRepository(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getAllShips(Specification<Ship> specification) {

        return shipRepository.findAll(specification);
    }

    @Override
    public Page<Ship> getAllShips(Specification<Ship> specification, Pageable sortedPageable) {
        return shipRepository.findAll(specification, sortedPageable);
    }


    @Override
    public Ship createShip(Ship requestShip) {
        if (requestShip.getName() == null || requestShip.getPlanet() == null
                || requestShip.getShipType() == null || requestShip.getProdDate() == null
                || requestShip.getSpeed() == null || requestShip.getCrewSize() == null) {
            throw new BadRequestException("One of ship parameters is null.");
        }

        shipValidation(requestShip);
        if (requestShip.getUsed() == null) {
            requestShip.setUsed(false);
        }

        Double rating = getShipRating(requestShip);
        requestShip.setRating(rating);
        return shipRepository.saveAndFlush(requestShip);

    }

    private Double getShipRating(Ship requestShip) {
        double k = 1;
        if (requestShip.getUsed()) {
            k = 0.5;
        }
        Date prodDate = requestShip.getProdDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prodDate);
        int prodYear = calendar.get(Calendar.YEAR);
        int currentYear = 3019;
        BigDecimal rating = new BigDecimal((80 * requestShip.getSpeed() * k) / (currentYear - prodYear + 1));
        rating = rating.setScale(2, RoundingMode.HALF_UP);
        return rating.doubleValue();
    }

    @Override
    public Ship getShipById(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundException("Ship not found");
        }
        return shipRepository.findById(id).get();
    }

    @Override
    public Ship updateShipById(Long id, Ship ship) {
        shipValidation(ship);
        if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundException("Ship not found");
        }
        Ship updatedShip = shipRepository.findById(id).get();
        if (ship.getName() != null) {
            updatedShip.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            updatedShip.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            updatedShip.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            updatedShip.setProdDate(ship.getProdDate());
        }
        if (ship.getUsed() != null) {
            updatedShip.setUsed(ship.getUsed());
        }
        if (ship.getSpeed() != null) {
            updatedShip.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            updatedShip.setCrewSize(ship.getCrewSize());
        }

        Double rating = getShipRating(updatedShip);
        updatedShip.setRating(rating);

        return shipRepository.save(updatedShip);
    }

    @Override
    public void deleteShipById(Long id) {

        if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundException("Ship not found");
        }
        shipRepository.deleteById(id);
    }


    @Override
    public Long getIdFromString(String id) {
        if (id.equals("") || id.equals("0") || id == null) {
            throw new BadRequestException("Ship not found");
        }
        try {
            Long longId = Long.parseLong(id);
            return longId;
        } catch (Exception exception) {
            throw new BadRequestException("Id is not correct.");
        }
    }

    private void shipValidation(Ship ship) {
        if (ship.getName() != null && (ship.getName().length() == 0 || ship.getName().length() > 50)) {
            throw new BadRequestException("Ship.name is not valid.");
        }

        if (ship.getPlanet() != null && (ship.getPlanet().length() == 0 || ship.getPlanet().length() > 50)) {
            throw new BadRequestException("Ship.planet is not valid.");
        }


        if (ship.getProdDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ship.getProdDate());
            if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019) {
                throw new BadRequestException("Ship.prodDate is not valid.");
            }
        }

        if (ship.getSpeed() != null && (ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99)) {
            throw new BadRequestException("Ship.speed is not valid.");
        }

        if (ship.getCrewSize() != null && (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999)) {
            throw new BadRequestException("Ship.crewSize is not valid.");
        }

    }

    @Override
    public Specification<Ship> filterByName(String name) {
        if (name == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%"));
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        if (planet == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("planet"), "%" + planet + "%"));
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        if (shipType == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("shipType"), shipType));
    }

    @Override
    public Specification<Ship> filterByProdDate(Long after, Long before) {
        if (before == null && after == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) -> {
            if (after == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), new Date(before));
            } else if (before == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(after));
            }

            return criteriaBuilder.between(root.get("prodDate"),
                    new Date(after), new Date(before));

        }
        );
    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        if (isUsed == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->{
            if(isUsed){
                return criteriaBuilder.isTrue(root.get("isUsed"));
            } else {
                return criteriaBuilder.isFalse(root.get("isUsed"));
            }
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        if (minSpeed == null && maxSpeed==null) {
            return null;
        }
        return ((root, query, criteriaBuilder) -> {
            if(minSpeed==null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            }
            else if(maxSpeed==null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);
            }
            return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
        });

    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        if (minCrewSize == null && maxCrewSize == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) -> {
            if(minCrewSize==null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            } else if(maxCrewSize==null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"),minCrewSize);
            } return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        });
    }

    @Override
    public Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        if (minRating == null && maxRating == null) {
            return null;
        }
        return ((root, query, criteriaBuilder) -> {
           if(minRating==null){
               return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
           } else if(maxRating==null){
               return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
           } return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
        });
    }

}
