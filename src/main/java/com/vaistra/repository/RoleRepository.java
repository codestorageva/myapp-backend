package com.vaistra.repository;


import com.vaistra.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>

{
    Boolean existsByRoleNameIgnoreCase(String roleName);

    List<Role> findAllByPermissions_PermissionId(int id);

//    Role findByRoleName(String roleName);

    Role findByRoleNameIgnoreCase(String roleName);

    Page<Role> findByRoleIdOrRoleNameContainingIgnoreCase(Integer intKeyword, String keyword, Pageable pageable);

//    Boolean existsByRoleIdAndPermissions_PermissionNameContainingIgnoreCase(Integer roleId, String permissionName);

//    Boolean existsByRoleIdAndPermissions_PermissionGroupContainingIgnoreCase(Integer roleId, String permissionGroup);
//    Boolean existsByRoleIdAndPermissions_PermissionGroupContainingIgnoreCaseAndPermissions_PermissionNameContainingIgnoreCase(Integer roleId, String permissionGroup, String permissionName);

    Boolean existsByRoleIdAndPermissions_PermissionName(Integer roleId, String permissionName);

    Boolean existsByRoleIdAndAndPermissions_PermissionGroupAndPermissions_PermissionName(Integer roleId, String permissionGroup, String permissionName);
}

