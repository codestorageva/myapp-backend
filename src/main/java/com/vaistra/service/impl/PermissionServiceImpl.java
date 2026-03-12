package com.vaistra.service.impl;

import com.vaistra.config.jwt.JwtService;
import com.vaistra.dto.PermissionDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.PermissionUpdateDto;
import com.vaistra.entity.Permission;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.PermissionRepository;
import com.vaistra.repository.RoleRepository;
import com.vaistra.service.PermissionService;
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
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService
{
    @PersistenceContext
    private EntityManager entityManager;

    private final PermissionRepository permissionRepository;

    private final PasswordEncoder passwordEncoder;
    private final AppUtils appUtils;
    private final JwtService jwtService;
//    private final AwsService awsService;
    private final RoleRepository roleRepository;

    public PermissionServiceImpl(EntityManager entityManager, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder, AppUtils appUtils, JwtService jwtService, RoleRepository roleRepository) {
        this.entityManager = entityManager;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUtils = appUtils;
        this.jwtService = jwtService;

        this.roleRepository = roleRepository;
    }

    @Override
    public MessageResponse addPermission(PermissionDto permissionDto) {
        if(permissionRepository.existsByPermissionNameIgnoreCase(permissionDto.getPermissionName()))
            throw new DuplicateEntryException("Permission with name '"+permissionDto.getPermissionName()+"' already exist!");

        Permission permission=new Permission();
        permission.setPermissionSuperGroup(permissionDto.getPermissionSuperGroup().toUpperCase());
        permission.setPermissionGroup(permissionDto.getPermissionGroup().toUpperCase());
        permission.setPermissionName(permissionDto.getPermissionName().toUpperCase());

        if(permissionDto.getGuardName()!=null){
            permission.setGuardName(permissionDto.getGuardName().toUpperCase());
        }
        else {
            permission.setGuardName("WEB");
        }
        permission.setIsDeleted(false);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permission.setDeletedAt(null);


        permissionRepository.save(permission);

        return new MessageResponse(true, HttpStatus.OK,"Permission saved.");
    }

    @Override
    public DataResponse getPermissionById(int id) {
        return new DataResponse(true,HttpStatus.OK, appUtils.permissionToDto(permissionRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("Permission with id '"+id+"' not found!"))));
    }

    @Override
    public HttpResponse getAllPermissions(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted)
    {
        Page<Permission> pagePermission = null;
        List<PermissionDto> permissions = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;

        if(isDeleted.equalsIgnoreCase("true")){
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        }catch (Exception ignored){}

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Permission> criteriaQuery = criteriaBuilder.createQuery(Permission.class);
            Root<Permission> root = criteriaQuery.from(Permission.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"),softDeleted);
            Predicate permissionIdPredicate = criteriaBuilder.equal(root.get("permissionId"),intKeyword);
            Predicate permissionNamePredicate = null;

            if(keyword!=null)
                permissionNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("permissionName").as(String.class)), "%" + keyword.toLowerCase() + "%");

            Predicate combinedPredicate = null;

            if(softDeleted!=null) {

                if (intKeyword != null) {
                    combinedPredicate = criteriaBuilder.and(permissionIdPredicate, deletedPredicate);  // Add state condition
                } else if (keyword != null) {
                    combinedPredicate = criteriaBuilder.and(permissionNamePredicate, deletedPredicate);  // Add state condition
                } else {
                    combinedPredicate = criteriaBuilder.and(deletedPredicate);  // Add state condition
                }
            }

            // Create the query to retrieve a page of results
            criteriaQuery.select(root)
                    .where(combinedPredicate);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<Permission> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pagePermission = new PageImpl<>(resultList, pageable, totalCount);

            permissions = appUtils.permissionToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pagePermission.getNumber())
                .pageSize(pagePermission.getSize())
                .totalElements(pagePermission.getTotalElements())
                .totalPages(pagePermission.getTotalPages())
                .isLastPage(pagePermission.isLast())
                .data(permissions)
                .build();
    }


    @Override
    public MessageResponse updatePermission(PermissionUpdateDto c, int id)
    {

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with Id '" + id + "' not found!"));

        // HANDLE DUPLICATE ENTRY EXCEPTION
        if(c.getPermissionName() != null)
        {
            Permission permissionWithSameName = permissionRepository.findByPermissionNameIgnoreCase(c.getPermissionName().trim());

            if(permissionWithSameName != null && !permissionWithSameName.getPermissionId().equals(permission.getPermissionId()))
                throw new DuplicateEntryException("Permission '"+c.getPermissionName()+"' already exist!");
            permission.setPermissionName(c.getPermissionName().trim().toUpperCase());
        }

        if(c.getPermissionGroup()!=null){
            permission.setPermissionGroup(c.getPermissionGroup().toUpperCase());
        }

        if(c.getPermissionSuperGroup()!=null){
            permission.setPermissionSuperGroup(c.getPermissionSuperGroup().toUpperCase());
        }
        permission.setIsDeleted(c.getIsDeleted());
        permission.setUpdatedAt(LocalDateTime.now());


        permissionRepository.save(permission);

        return new MessageResponse(true,HttpStatus.OK, "Permission-Data updated");
    }


/*    @Override
    public MessageResponse deletePermissionById(int id) {
        Permission permission=permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission with Id '" + id + "' not found!"));

        List<Role> roles = new ArrayList<>(roleRepository.findAllByPermissions_PermissionId(id));

        if(!roles.isEmpty())
            throw new ResourceAlreadyExistException("Permission: " + permission.getPermissionName() + " exist in already created roles. please remove first from that roles");

        permission.setIsDeleted(true);
        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.delete(permission);
        return new MessageResponse(true,HttpStatus.OK, "Permission with Id '" + id + "' hard deleted");
    }*/

    @Override
    public MessageResponse softDeletePermissionById(int permissionId) {
        Permission permission  = permissionRepository.findById(permissionId).orElseThrow(() -> new ResourceNotFoundException("Permission with Id '" + permissionId + "' not found!"));
        permission.setIsDeleted(true);
        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.save(permission);
        return new MessageResponse(true,HttpStatus.OK, "Permission with Id '" + permissionId + "' Soft Deleted");
    }

    @Override
    public MessageResponse restorePermissionById(int permissionId) {
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(() -> new ResourceNotFoundException("Permission with Id '" + permissionId + "' not found!"));
        permission.setIsDeleted(false);
        permission.setDeletedAt(null);
        permissionRepository.save(permission);
        return new MessageResponse(true,HttpStatus.OK,"Permission with id '" + permissionId + "' restored!");
    }

    @Override
    public ListResponse getAllPermissionByNonSoftDeleted() {
        List<PermissionDto> permissions = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Permission> criteriaQuery = criteriaBuilder.createQuery(Permission.class);
            Root<Permission> root = criteriaQuery.from(Permission.class);

            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"),false);
            criteriaQuery.select(root)
                    .where(deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("permissionSuperGroup")));

            List<Permission> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            permissions = appUtils.permissionToDtos(resultList);

        } catch (NoResultException ignored) {}

        return new ListResponse(true,HttpStatus.OK,permissions);
    }

    @Override
    public PermissionGroupResponseDto getAllPermissionsByPermissionGroup(
            String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted) {

        Page<Permission> pagePermission = null;
        List<PermissionSuperGroupResponse> permissionBySuperGroup = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;

        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {}

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Permission> criteriaQuery = criteriaBuilder.createQuery(Permission.class);
            Root<Permission> root = criteriaQuery.from(Permission.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate permissionIdPredicate = criteriaBuilder.equal(root.get("permissionId"), intKeyword);
            Predicate permissionNamePredicate = null;

            if (keyword != null) {
                permissionNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("permissionName").as(String.class)),
                        "%" + keyword.toLowerCase() + "%"
                );
            }

            Predicate combinedPredicate = null;

            if (softDeleted != null) {
                if (intKeyword != null) {
                    combinedPredicate = criteriaBuilder.and(permissionIdPredicate, deletedPredicate);
                } else if (keyword != null) {
                    combinedPredicate = criteriaBuilder.and(permissionNamePredicate, deletedPredicate);
                } else {
                    combinedPredicate = criteriaBuilder.and(deletedPredicate);
                }
            }

            criteriaQuery.select(root).where(combinedPredicate);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<Permission> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pagePermission = new PageImpl<>(resultList, pageable, totalCount);

            permissionBySuperGroup = appUtils.permissionGroupToDtos(resultList);

        } catch (NoResultException ignored) {}

        return PermissionGroupResponseDto.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pagePermission.getNumber())
                .pageSize(pagePermission.getSize())
                .totalElements(pagePermission.getTotalElements())
                .totalPages(pagePermission.getTotalPages())
                .isLastPage(pagePermission.isLast())
                .data(permissionBySuperGroup)
                .build();
    }

    @Override
    public LongResponse getTotalPermission() {
        Long count = permissionRepository.count();
        return new LongResponse(true,HttpStatus.OK,count);
    }

}

