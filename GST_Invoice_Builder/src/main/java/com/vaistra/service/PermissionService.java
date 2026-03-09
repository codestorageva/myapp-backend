package com.vaistra.service;

import com.vaistra.dto.PermissionDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.PermissionUpdateDto;

public interface PermissionService {
    MessageResponse addPermission(PermissionDto permissionDto);
    DataResponse getPermissionById(int id);
    HttpResponse getAllPermissions(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted);
    MessageResponse updatePermission(PermissionUpdateDto c, int id);
//    MessageResponse deletePermissionById(int id);
    MessageResponse softDeletePermissionById(int permissionId);
    MessageResponse restorePermissionById(int permissionId);
    ListResponse getAllPermissionByNonSoftDeleted();
    PermissionGroupResponseDto getAllPermissionsByPermissionGroup(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted);

    LongResponse getTotalPermission();
}

