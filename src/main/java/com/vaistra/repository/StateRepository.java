package com.vaistra.repository;


import com.vaistra.entity.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {

      State findByStateNameIgnoreCase(String stateName);

      //    State findByStateNameIgnoreCase(String name);
     //    List<State> findAllByStateNameIgnoreCase(String name);
    //    boolean existsByStateName(String name);
    //    List<State> findAllByCountry_CountryId(Integer countryId);
   //    Page<State> findAllByStateIdOrStatusOrStateName(Integer stateId, Boolean status, String stateName, String countryName, Pageable p);
  //    Page<State> findAllByIsDeleted(Boolean b, Pageable p);
 //    List<State> findAllByStatusAndIsDeleted(Boolean b1, Boolean b2);

      boolean existsByStateNameIgnoreCase(String stateName);

}
