package com.vaistra.service;

import com.vaistra.dto.RoleDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.RoleUpdateDto;
import com.vaistra.entity.User;

public interface RoleService
{
    MessageResponse addRole(RoleDto roleDto);
    DataResponse getRoleById(int id);
    HttpResponse getAllRoles(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted);
    MessageResponse updateRole(RoleUpdateDto dto, int id);
//    MessageResponse deleteRoleById(int id);
    MessageResponse softDeleteRoleById(int permissionId);
    MessageResponse restoreRoleById(int permissionId);
    ListResponse exportedRoleData();
    ListResponse getAllRoleByNonSoftDeleted();
    ListResponse getAllRoleByNonSoftDeletedAndCurrentUserRole(User loggedInUser);
    BooleanResponse checkPermissionByRoleId(User loggedInUser, String permissionGroup, String permissionName);
    BooleanResponse checkPermission(User loggedInUser, String permissionName);
    BooleanResponse checkAuth(User loggedInUser);
    LongResponse getTotalRole();
}

