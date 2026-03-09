package com.vaistra.repository;


import com.vaistra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Boolean existsByEmailIgnoreCase(String email);

    User findByEmailIgnoreCase(String email);

    List<User> findAllByRole_RoleId(int id);

    List<User> findAllByIsDeleted(Boolean b);

    Page<User> findByIsDeleted(boolean b, Pageable pageable);

    Boolean existsByJwtTokenIgnoreCase(String jwtToken);

    Boolean existsByEmailIgnoreCaseAndFullNameIgnoreCaseNot(String email, String fullName);


}

