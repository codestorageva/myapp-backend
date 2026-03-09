package com.vaistra.repository;

import com.vaistra.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Integer> {

    Boolean existsByPermissionNameIgnoreCase(String permissionName);

    Permission findByPermissionNameIgnoreCase(String permissionName);

    Page<Permission> findByPermissionIdOrPermissionNameContainingIgnoreCase(Integer intKeyword, String keyword, Pageable pageable);
}