package com.vaistra.repository;

import com.vaistra.entity.City;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City,Integer> {

    boolean existsByCityNameIgnoreCaseAndState_StateNameIgnoreCase(String cityName, String stateName);

    City findByCityNameIgnoreCaseAndState_StateNameIgnoreCase(String cityName, String stateName);
}
