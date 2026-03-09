package com.vaistra.service.impl;

import com.vaistra.config.jwt.JwtService;
import com.vaistra.dto.RoleDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.RoleUpdateDto;
import com.vaistra.entity.Permission;
import com.vaistra.entity.Role;
import com.vaistra.entity.User;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.exception.UserUnauthorizedException;
import com.vaistra.repository.PermissionRepository;
import com.vaistra.repository.RoleRepository;
import com.vaistra.repository.UserRepository;
import com.vaistra.service.RoleService;
import com.vaistra.service.UserService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @PersistenceContext
    private EntityManager entityManager;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUtils appUtils;
    private final JwtService jwtService;
//    private final AwsService awsService;
    private final UserRepository userRepository;
    private final UserService userService;

    public RoleServiceImpl(EntityManager entityManager, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder, AppUtils appUtils, JwtService jwtService, UserRepository userRepository, UserService userService) {
        this.entityManager = entityManager;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUtils = appUtils;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public MessageResponse addRole(RoleDto roleDto) {
        // Check if role already exists
        if (roleRepository.existsByRoleNameIgnoreCase("ROLE_" + roleDto.getRoleName())) {
            throw new DuplicateEntryException("Role with name '" + roleDto.getRoleName() + "' already exists!");
        }

        // Create Role object
        Role role = new Role();
        role.setRoleName("ROLE_" + roleDto.getRoleName().toUpperCase());
        role.setGuardName(roleDto.getGuardName() != null ? roleDto.getGuardName().toUpperCase() : "WEB");
        role.setIsDeleted(false);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeletedAt(null);

        // Convert PermissionResponseDto list to permission ID list
        List<Integer> permissionIds = roleDto.getPermissions().stream()
                .map(PermissionResponseDto::getPermissionId)  // Extract only permissionId
                .collect(Collectors.toList());

        // Fetch permissions in a single query
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        // Set to store additional permissions (avoiding duplicates)
        Set<Permission> additionalPermissions = new HashSet<>();

        for (Permission permission : permissions) {
            // Add DATABASE_READ if the permission belongs to specific groups
            if (!"DATABASE_READ".equalsIgnoreCase(permission.getPermissionName()) &&
                    List.of("COUNTRY", "STATE", "DISTRICT", "SUB_DISTRICT", "VILLAGE", "DESIGNATION", "ENTITY",
                            "EQUIPMENT_TYPE", "MINERAL", "VEHICLE_TYPE").contains(permission.getPermissionGroup())) {

                Permission databaseReadPermission = permissionRepository.findByPermissionNameIgnoreCase("DATABASE_READ");
                if (databaseReadPermission != null && !permissions.contains(databaseReadPermission)) {
                    additionalPermissions.add(databaseReadPermission);
                }
            }

            // Add USERTYPE_READ if the permission belongs to specific groups
            if (!"USERTYPE_READ".equalsIgnoreCase(permission.getPermissionName()) &&
                    List.of("EQUIPMENT", "EQUIPMENT_OPERATOR", "EQUIPMENT_OWNER", "FUEL_STATION", "MATERIAL_PURCHASER",
                            "MATERIAL_SUPPLIER", "MINING_AGENT", "MINING_CONTRACTOR", "MINING_LEASE", "QUARRY_LEASE",
                            "STOCK_REGISTRATION", "TRANSPORT_AGENT", "TRANSPORTER", "VEHICLE", "VEHICLE_DRIVER",
                            "VEHICLE_OWNER", "WEIGH_BRIDGE").contains(permission.getPermissionGroup())) {

                Permission userTypeReadPermission = permissionRepository.findByPermissionNameIgnoreCase("USERTYPE_READ");
                if (userTypeReadPermission != null && !permissions.contains(userTypeReadPermission)) {
                    additionalPermissions.add(userTypeReadPermission);
                }
            }
        }

        // Merge additional permissions (ensuring no duplicates)
        permissions.addAll(additionalPermissions);

        // Assign permissions and save role
        role.setPermissions(new ArrayList<>(permissions));
        roleRepository.save(role);

        // Return success message
        return new MessageResponse(true, HttpStatus.OK, "Role saved successfully.");
    }

    @Override
    public DataResponse getRoleById(int id) {
        return new DataResponse(true, HttpStatus.OK, appUtils.roleToDtoBasedOnPermissionGroup(roleRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Role with id '" + id + "' not found!"))));
    }

    @Override
    public HttpResponse getAllRoles(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted) {
        Page<Role> pageRole = null;
        List<RoleDto> roles = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;

        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }


        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);


            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate roleIdPredicate = criteriaBuilder.equal(root.get("roleId"), intKeyword);
            Predicate rolePredicate = null;

            if (keyword != null) {
                rolePredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("roleName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("guardName").as(String.class)), "%" + keyword.toLowerCase() + "%")
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("permissions").get("permissionName")).as(String.class), "%" + keyword.toLowerCase() + "%")
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("permissions").get("sideMenu").get("sideMenuName")).as(String.class), "%" + keyword.toLowerCase() + "%")
                );
            }

            Predicate combinedPredicate = null;

            if (softDeleted != null) {
                if (intKeyword != null) {
                    combinedPredicate = criteriaBuilder.and(roleIdPredicate, deletedPredicate);  // Add state condition
                } else if (keyword != null) {
                    combinedPredicate = criteriaBuilder.and(rolePredicate, deletedPredicate);  // Add state condition
                } else {
                    combinedPredicate = criteriaBuilder.and(deletedPredicate);  // Add state condition
                }
            }

            criteriaQuery.select(root)
                    .where(criteriaBuilder.and(combinedPredicate));

            // Create the query to retrieve a page of results
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<Role> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pageRole = new PageImpl<>(resultList, pageable, totalCount);

            roles = appUtils.roleToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageRole.getNumber())
                .pageSize(pageRole.getSize())
                .totalElements(pageRole.getTotalElements())
                .totalPages(pageRole.getTotalPages())
                .isLastPage(pageRole.isLast())
                .data(roles)
                .build();
    }

    @Override
    public MessageResponse updateRole(RoleUpdateDto dto, int id) {
        // Handle if role exists by ID
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with Id '" + id + "' not found!"));

        // Handle duplicate role name exception
        if (dto.getRoleName() != null) {
            Role roleWithSameName = roleRepository.findByRoleNameIgnoreCase("ROLE_" + dto.getRoleName().trim());
            if (roleWithSameName != null && !roleWithSameName.getRoleId().equals(role.getRoleId())) {
                throw new DuplicateEntryException("Role '" + dto.getRoleName() + "' already exists!");
            }
            role.setRoleName("ROLE_" + dto.getRoleName().trim().toUpperCase());
        }

        // Remove duplicate permission IDs before processing
        List<Integer> uniquePermissionIds = dto.getPermissionIds().stream()
                .distinct() // Removes duplicates
                .collect(Collectors.toList());

        // Fetch permissions in a single query
        List<Permission> updatedPermissions = permissionRepository.findAllById(uniquePermissionIds);

        // Set to store additional permissions (avoiding duplicates)
        Set<Permission> additionalPermissions = new HashSet<>();

        for (Permission permission : updatedPermissions) {
            // Add DATABASE_READ if required
            if (List.of("COUNTRY", "STATE", "DISTRICT", "SUB_DISTRICT", "VILLAGE", "DESIGNATION", "ENTITY",
                    "EQUIPMENT_TYPE", "MINERAL", "VEHICLE_TYPE").contains(permission.getPermissionGroup())) {

                Permission databaseRead = permissionRepository.findByPermissionNameIgnoreCase("DATABASE_READ");
                if (databaseRead != null) {
                    additionalPermissions.add(databaseRead);
                }
            }

            // Add USERTYPE_READ if required
            if (List.of("EQUIPMENT", "EQUIPMENT_OPERATOR", "EQUIPMENT_OWNER", "FUEL_STATION", "MATERIAL_PURCHASER",
                    "MATERIAL_SUPPLIER", "MINING_AGENT", "MINING_CONTRACTOR", "MINING_LEASE", "QUARRY_LEASE",
                    "STOCK_REGISTRATION", "TRANSPORT_AGENT", "TRANSPORTER", "VEHICLE", "VEHICLE_DRIVER",
                    "VEHICLE_OWNER", "WEIGH_BRIDGE").contains(permission.getPermissionGroup())) {

                Permission userTypeRead = permissionRepository.findByPermissionNameIgnoreCase("USERTYPE_READ");
                if (userTypeRead != null) {
                    additionalPermissions.add(userTypeRead);
                }
            }
        }

        // Merge additional permissions (ensuring no duplicates)
        updatedPermissions = new ArrayList<>(new HashSet<>(updatedPermissions)); // Remove any accidental duplicates
        updatedPermissions.addAll(additionalPermissions);

        // Assign updated permissions to the role
        role.setPermissions(updatedPermissions);

        // Handle deletion flag
        role.setIsDeleted(dto.getIsDeleted() != null ? dto.getIsDeleted() : false);

        // Update role's updatedAt timestamp
        role.setUpdatedAt(LocalDateTime.now());

        // Save the role with updated information
        roleRepository.save(role);

        // Invalidate JWT tokens for users with this role
        List<User> users = userRepository.findAllByRole_RoleId(id);
        users.forEach(user -> {
            user.setJwtToken(null);
            user.setIsLoggedOut(true);
        });
        userRepository.saveAll(users);

        return new MessageResponse(true, HttpStatus.OK, "Role-Data updated successfully.");
    }


  /*  @Override
    public MessageResponse deleteRoleById(int id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with Id '" + id + "' not found!"));


        List<User> users = new ArrayList<>(userRepository.findAllByRole_RoleId(id));

        if (!users.isEmpty())
            throw new ResourceAlreadyExistException("Role: " + role.getRoleName() + " is assigned in already created users. please remove that users first...!");

        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.delete(role);
        return new MessageResponse(true, HttpStatus.OK, "Role with Id '" + id + "' hard deleted");
    }*/

    @Override
    public MessageResponse softDeleteRoleById(int roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role with Id '" + roleId + "' not found!"));

        List<User> users = new ArrayList<>(userRepository.findAllByRole_RoleId(roleId));

        for (User user : users) {
            userService.logout(user);
        }
        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.save(role);
        return new MessageResponse(true, HttpStatus.OK, "Role with Id '" + roleId + "' Soft Deleted");
    }

    @Override
    public MessageResponse restoreRoleById(int roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role with Id '" + roleId + "' not found!"));
        role.setIsDeleted(false);
        role.setDeletedAt(null);
        roleRepository.save(role);
        return new MessageResponse(true, HttpStatus.OK, "Role with id '" + roleId + "' restored!");
    }

    @Override
    public ListResponse exportedRoleData() {
        List<RoleExportDTO> roles = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("roleName")));
            ;

            List<Role> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            roles = appUtils.roleExportDTOStoRoles(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, roles);
    }

    @Override
    public ListResponse getAllRoleByNonSoftDeleted() {
        List<RoleResponseDto> roles = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);

            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);
            criteriaQuery.select(root)
                    .where(deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("roleName")));

            List<Role> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            roles = appUtils.roleResponseToDtos(resultList);

        } catch (NoResultException ignored) {

        }
        return new ListResponse(true, HttpStatus.OK, roles);
    }

    @Override
    public ListResponse getAllRoleByNonSoftDeletedAndCurrentUserRole(User loggedInUser) {
        List<PermissionsBasedOnRoleDTO> roles = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Role> criteriaQuery = criteriaBuilder.createQuery(Role.class);
            Root<Role> root = criteriaQuery.from(Role.class);

            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);
            Predicate rolePredicate = criteriaBuilder.equal(root.get("roleId"), loggedInUser.getRole().getRoleId());

            Predicate finalPredicate = criteriaBuilder.and(deletedPredicate, rolePredicate);

            criteriaQuery.select(root)
                    .where(finalPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("roleName")));

            List<Role> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            roles = appUtils.rolesPermissionToDtos(resultList);

        } catch (NoResultException ignored) {

        }
        return new ListResponse(true, HttpStatus.OK, roles);
    }

    @Override
    public BooleanResponse checkPermissionByRoleId(User loggedInUser, String permissionGroup, String permissionName) {

        Integer roleId = loggedInUser.getRole().getRoleId();

        Boolean isPermission = false;

        if (roleId == 1 && permissionGroup.equalsIgnoreCase("PERMISSION")) {
            isPermission = true;
            return new BooleanResponse(true, HttpStatus.OK, isPermission);
        }
        if (roleId == 1 && permissionGroup.equalsIgnoreCase("ROLE")) {
            isPermission = true;
            return new BooleanResponse(true, HttpStatus.OK, isPermission);
        }
        if (permissionName.trim().equalsIgnoreCase("read")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "READ";
        }
        if (permissionName.trim().equalsIgnoreCase("add")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "WRITE";
        }
        if (permissionName.trim().equalsIgnoreCase("edit")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "UPDATE";
        }
        if (permissionName.trim().equalsIgnoreCase("delete")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "DELETE";
        }
        if (permissionName.trim().equalsIgnoreCase("restore")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "RESTORE";
        }
        if (permissionName.trim().equalsIgnoreCase("import")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "IMPORT";
        }
        if (permissionName.trim().equalsIgnoreCase("export")) {
            permissionName = permissionGroup.toUpperCase().trim() + "_" + "EXPORT";
        }

        isPermission = roleRepository.existsByRoleIdAndAndPermissions_PermissionGroupAndPermissions_PermissionName(roleId, permissionGroup, permissionName);

        return new BooleanResponse(true, HttpStatus.OK, isPermission);
    }

    @Override
    public BooleanResponse checkPermission(User loggedInUser, String permissionName) {
        Boolean isPermission;

        isPermission = roleRepository.existsByRoleIdAndPermissions_PermissionName(loggedInUser.getRole().getRoleId(), permissionName);

        return new BooleanResponse(true, HttpStatus.OK, isPermission);
    }

    @Override
    public BooleanResponse checkAuth(User loggedInUser) {
        Boolean user = userRepository.existsByJwtTokenIgnoreCase(loggedInUser.getJwtToken());

        if(user.equals(false)){
            throw new UserUnauthorizedException("Permission to access this site updated, Please login again...!");
        }

        return new BooleanResponse(true,HttpStatus.OK,user);
    }

    @Override
    public LongResponse getTotalRole() {
        Long count = roleRepository.count();
        return new LongResponse(true,HttpStatus.OK,count);
    }
}

