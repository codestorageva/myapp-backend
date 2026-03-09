package com.vaistra.util;

import com.vaistra.dto.*;
import com.vaistra.dto.response.*;
import com.vaistra.entity.*;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.StateRepository;
import com.vaistra.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppUtils {
    private final EntityManager entityManager;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;


    public AppUtils(EntityManager entityManager, ModelMapper modelMapper, UserRepository userRepository, CityRepository cityRepository, StateRepository stateRepository) {
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
    }

    //---------------------------------------------------Permission Utils----------------------------------------------------
    public PermissionDto permissionToDto(Permission p) {
        return new PermissionDto(
                p.getPermissionId() != null ? p.getPermissionId() : null,
                p.getPermissionSuperGroup() != null ? p.getPermissionSuperGroup() : null,
                p.getPermissionGroup() != null ? p.getPermissionGroup() : null,
                p.getPermissionName() != null ? p.getPermissionName() : null,
                p.getGuardName() != null ? p.getGuardName() : null,
                p.getIsDeleted() != null ? p.getIsDeleted() : null,
                p.getCreatedAt() != null ? p.getCreatedAt() : null,
                p.getUpdatedAt() != null ? p.getUpdatedAt() : null,
                p.getDeletedAt() != null ? p.getDeletedAt() : null

        );
    }

    public List<PermissionDto> permissionToDtos(List<Permission> pl) {
        List<PermissionDto> dtos = new ArrayList<>();

        for (Permission p : pl) {
            dtos.add(new PermissionDto(
                    p.getPermissionId() != null ? p.getPermissionId() : null,
                    p.getPermissionSuperGroup() != null ? p.getPermissionSuperGroup() : null,
                    p.getPermissionGroup() != null ? p.getPermissionGroup() : null,
                    p.getPermissionName() != null ? p.getPermissionName() : null,
                    p.getGuardName() != null ? p.getGuardName() : null,
                    p.getIsDeleted() != null ? p.getIsDeleted() : null,
                    p.getCreatedAt() != null ? p.getCreatedAt() : null,
                    p.getUpdatedAt() != null ? p.getUpdatedAt() : null,
                    p.getDeletedAt() != null ? p.getDeletedAt() : null
            ));
        }
        return dtos;
    }

    public List<PermissionSuperGroupResponse> permissionGroupToDtos(List<Permission> pl) {

        // First, group permissions by supergroup
        Map<String, List<Permission>> superGroupMap = pl.stream()
                .collect(Collectors.groupingBy(Permission::getPermissionSuperGroup));

        List<PermissionSuperGroupResponse> superGroupDtos = new ArrayList<>();

        // Now, iterate over each supergroup
        for (Map.Entry<String, List<Permission>> superGroupEntry : superGroupMap.entrySet()) {
            String currentSuperGroup = superGroupEntry.getKey();
            List<Permission> permissionsInSuperGroup = superGroupEntry.getValue();

            // Group permissions within this supergroup by permission group
            Map<String, List<Map<String, Object>>> groupMap = permissionsInSuperGroup.stream()
                    .collect(Collectors.groupingBy(
                            Permission::getPermissionGroup,
                            Collectors.mapping(p -> {
                                Map<String, Object> permissionMap = new HashMap<>();
                                permissionMap.put("permissionId", p.getPermissionId());
                                permissionMap.put("permissionName", p.getPermissionName());
                                return permissionMap;
                            }, Collectors.toList())
                    ));

            // Convert the grouped permissions into PermissionGroupResponse objects
            List<PermissionGroupResponse> groupResponses = groupMap.entrySet().stream()
                    .map(entry -> new PermissionGroupResponse(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            // Add the supergroup to the result
            superGroupDtos.add(new PermissionSuperGroupResponse(currentSuperGroup, groupResponses));
        }

        return superGroupDtos;
    }
    //---------------------------------------------------Role Utils----------------------------------------------------

    public RoleDto roleToDto(Role r) {
        return new RoleDto(
                r.getRoleId(),
                r.getRoleName(),
                r.getGuardName(),
                r.getIsDeleted(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getDeletedAt(),
                r.getPermissions() != null
                        ? r.getPermissions().stream()
                        .map(permission -> new PermissionResponseDto(permission.getPermissionId(),
                                permission.getPermissionName()))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    public List<RoleDto> roleToDtos(List<Role> rl) {
        return rl.stream().map(r -> new RoleDto(
                r.getRoleId(),
                r.getRoleName(),
                r.getGuardName(),
                r.getIsDeleted(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getDeletedAt(),
                r.getPermissions() != null
                        ? r.getPermissions().stream()
                        .map(permission -> new PermissionResponseDto(permission.getPermissionId(),
                                permission.getPermissionName()))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        )).collect(Collectors.toList());
    }

    public List<RoleResponseDto> roleResponseToDtos(List<Role> rl) {
        return rl.stream()
                .map(r -> new RoleResponseDto(r.getRoleId(), r.getRoleName()))
                .collect(Collectors.toList());
    }


/*
    public List<PermissionSuperGroupResponse> rolesPermissionToDtos(List<Role> roles) {
        // Use a Map to group by permissionSuperGroup and then by permissionGroup
        Map<String, Map<String, List<PermissionsBasedOnRoleDTO>>> superGroupMap = new HashMap<>();

        for (Role role : roles) {
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    // Group by permissionSuperGroup
                    superGroupMap
                            .computeIfAbsent(permission.getPermissionSuperGroup(), k -> new HashMap<>())
                            // Group by permissionGroup within the supergroup
                            .computeIfAbsent(permission.getPermissionGroup(), k -> new ArrayList<>())
                            // Add permission to the respective group
                            .add(new PermissionsBasedOnRoleDTO(
                                    permission.getPermissionId(),
                                    permission.getPermissionSuperGroup(),
                                    permission.getPermissionGroup(),
                                    permission.getPermissionName(),
                                    permission.getGuardName()
                            ));
                }
            }
        }

        // Convert Map structure to the desired JSON response structure
        List<PermissionSuperGroupResponse> superGroupList = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<PermissionsBasedOnRoleDTO>>> superGroupEntry : superGroupMap.entrySet()) {
            List<PermissionGroupResponse> groupList = new ArrayList<>();
            for (Map.Entry<String, List<PermissionsBasedOnRoleDTO>> groupEntry : superGroupEntry.getValue().entrySet()) {
                groupList.add(new PermissionGroupResponse(
                        groupEntry.getKey(),
                        groupEntry.getValue()  // permissionData is a list of PermissionsBasedOnRoleDTO
                ));
            }
            superGroupList.add(new PermissionSuperGroupResponse(
                    superGroupEntry.getKey(),
                    groupList  // permissionGroups is a list of PermissionGroupResponse
            ));
        }

        return superGroupList;
    }
*/


    public List<PermissionsBasedOnRoleDTO> rolesPermissionToDtos(List<Role> roles) {
        List<PermissionsBasedOnRoleDTO> dtos = new ArrayList<>();

        for (Role role : roles) {
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    PermissionsBasedOnRoleDTO permissionDTO = new PermissionsBasedOnRoleDTO(
                            permission.getPermissionId(),
                            permission.getPermissionGroup(),
                            permission.getPermissionSuperGroup(),
                            permission.getPermissionName(),
                            permission.getGuardName()
                    );
                    dtos.add(permissionDTO);
                }
            }
        }
        return dtos;
    }


    public List<RoleExportDTO> roleExportDTOStoRoles(List<Role> roles) {
        List<RoleExportDTO> dtos = new ArrayList<>();
        for (Role r : roles) {
            dtos.add(new RoleExportDTO(
                    r.getRoleId(), r.getRoleName(), r.getGuardName(), r.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }

    public RoleDtoBasedOnPermissionGroup roleToDtoBasedOnPermissionGroup(Role role) {
        RoleDtoBasedOnPermissionGroup roleDto = new RoleDtoBasedOnPermissionGroup();
        roleDto.setRoleId(role.getRoleId());
        roleDto.setRoleName(role.getRoleName());
        roleDto.setGuardName(role.getGuardName());
        roleDto.setIsDeleted(role.getIsDeleted());
        roleDto.setCreatedAt(role.getCreatedAt());
        roleDto.setUpdatedAt(role.getUpdatedAt());
        roleDto.setDeletedAt(role.getDeletedAt());

        // Group permissions by permissionSuperGroup and permissionGroup
        Map<String, Map<String, List<PermissionsBasedOnRoleDTO>>> superGroupMap = new HashMap<>();

        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                String permissionSuperGroup = permission.getPermissionSuperGroup();
                String permissionGroup = permission.getPermissionGroup();

                PermissionsBasedOnRoleDTO permissionDTO = new PermissionsBasedOnRoleDTO(
                        permission.getPermissionId(),
                        permission.getPermissionSuperGroup(),
                        permission.getPermissionGroup(),
                        permission.getPermissionName(),
                        permission.getGuardName()
                );

                // Group by superGroup -> group -> permissions
                superGroupMap
                        .computeIfAbsent(permissionSuperGroup, sg -> new HashMap<>())  // SuperGroup Map
                        .computeIfAbsent(permissionGroup, g -> new ArrayList<>())      // Group Map
                        .add(permissionDTO);
            }
        }

        // Create a list of PermissionSuperGroupResponse objects
        List<PermissionSuperGroupResponse> permissionBasedOnSuperGroup = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<PermissionsBasedOnRoleDTO>>> superGroupEntry : superGroupMap.entrySet()) {
            String superGroupName = superGroupEntry.getKey();
            Map<String, List<PermissionsBasedOnRoleDTO>> groupMap = superGroupEntry.getValue();

            List<PermissionGroupResponse> groupResponses = new ArrayList<>();
            for (Map.Entry<String, List<PermissionsBasedOnRoleDTO>> groupEntry : groupMap.entrySet()) {
                String groupName = groupEntry.getKey();
                List<PermissionsBasedOnRoleDTO> permissions = groupEntry.getValue();

                PermissionGroupResponse groupResponse = new PermissionGroupResponse(groupName, permissions);
                groupResponses.add(groupResponse);
            }

            PermissionSuperGroupResponse superGroupResponse = new PermissionSuperGroupResponse(superGroupName, groupResponses);
            permissionBasedOnSuperGroup.add(superGroupResponse);
        }

        roleDto.setPermissionBasedOnSuperGroup(permissionBasedOnSuperGroup);
        return roleDto;
    }
    //---------------------------------------------------User Utils----------------------------------------------------

    public UserDTO userToDto(User u) {
        return new UserDTO(
                u.getUserId() != null ? u.getUserId() : null,
                u.getFullName() != null ? u.getFullName() : null,
                u.getEmail() != null ? u.getEmail() : null,
                u.getPassword() != null ? u.getPassword() : null,
                u.getDob() != null ? u.getDob() : null,
                u.getMobNo() != null ? u.getMobNo() : null,
                u.getAddressLine1() != null ? u.getAddressLine1() : null,
                u.getAddressLine2() != null ? u.getAddressLine2() : null,
                u.getAddressLine3() != null ? u.getAddressLine3() : null,
                u.getMessage() != null ? u.getMessage() : null,
                u.getRole() != null ? u.getRole().getRoleId() : null,
                u.getRole() != null ? u.getRole().getRoleName() : null,
                u.getIsDeleted() != null ? u.getIsDeleted() : null,
                u.getActiveStatus() != null ? u.getActiveStatus() : null,
                u.getCreatedAt() != null ? u.getCreatedAt() : null,
                u.getUpdatedAt() != null ? u.getUpdatedAt() : null,
                u.getDeletedAt() != null ? u.getDeletedAt() : null

        );
    }

    public List<UserDTO> usersToDtos(List<User> ul) {
        List<UserDTO> dtos = new ArrayList<>();

        for (User u : ul) {
            dtos.add(new UserDTO(
                    u.getUserId() != null ? u.getUserId() : null,
                    u.getFullName() != null ? u.getFullName() : null,
                    u.getEmail() != null ? u.getEmail() : null,
                    u.getPassword() != null ? u.getPassword() : null,
                    u.getDob() != null ? u.getDob() : null,
                    u.getMobNo() != null ? u.getMobNo() : null,
                    u.getAddressLine1() != null ? u.getAddressLine1() : null,
                    u.getAddressLine2() != null ? u.getAddressLine2() : null,
                    u.getAddressLine3() != null ? u.getAddressLine3() : null,
                    u.getMessage() != null ? u.getMessage() : null,
                    u.getRole() != null ? u.getRole().getRoleId() : null,
                    u.getRole() != null ? u.getRole().getRoleName() : null,
                    u.getIsDeleted() != null ? u.getIsDeleted() : null,
                    u.getActiveStatus() != null ? u.getActiveStatus() : null,
                    u.getCreatedAt() != null ? u.getCreatedAt() : null,
                    u.getUpdatedAt() != null ? u.getUpdatedAt() : null,
                    u.getDeletedAt() != null ? u.getDeletedAt() : null
            ));

        }
        return dtos;
    }

    public List<UserExportDTO> userExportDTOStoUsers(List<User> users) {
        List<UserExportDTO> dtos = new ArrayList<>();
        for (User r : users) {
            dtos.add(new UserExportDTO(
                    r.getUserId(), r.getFullName(), r.getEmail(), r.getPassword(), r.getMobNo(), r.getAddressLine1(), r.getAddressLine2(), r.getAddressLine3(), r.getRole().getRoleName(), r.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }

    public boolean isSupportedExtension(String ext) {
        int i = ext.lastIndexOf(".");

        String extension = "";

        if (i != -1) {
            extension = ext.substring(i + 1);
        }

        return extension != null && (
                extension.equals("png")
                        || extension.equals("jpg")
                        || extension.equals("jpeg")
                        || extension.equals("pdf"))
                || extension.equals("JPG")
                || extension.equals("JPEG")
                || extension.equals("PDF")
                || extension.equals("PNG");
    }

    public boolean isSupportedExtensionBatch(String ext) {
        int i = ext.lastIndexOf(".");

        String extension = "";

        if (i != -1) {
            extension = ext.substring(i + 1);
        }

        return extension != null && (
                extension.equals("csv")
                        || extension.equals("excel"))
                || extension.equals("CSV");
    }

    //---------------------------------------------------Company Utils----------------------------------------------------

    public static CompanyDto companyToDto(CompanyRegistration company) {

        List<BankDetailsDto> bankDetailsList = company.getBankDetails() != null
                ? company.getBankDetails().stream()
                .map(bank -> new BankDetailsDto(
                        bank.getBankId(),
                        bank.getBankName(),
                        bank.getIfscCode(),
                        bank.getBranch(),
                        bank.getAccountNumber(),
                        bank.getAccHolderName(),
                        bank.getBankAddress(),
                        bank.getStatus(),
                        bank.getIsDeleted(),
                        bank.getCreatedAt(),
                        bank.getUpdatedAt(),
                        bank.getDeletedAt(),
                        company.getCompanyId()
                )).toList()
                : List.of();

        List<InvoiceDto> invoiceList = company.getInvoices() != null
                ? company.getInvoices().stream()
                .map(invoice -> mapInvoiceToDto(invoice, company.getCompanyId()))
                .toList()
                : List.of();


        return new CompanyDto(

                // Basic Info
                company.getCompanyId(),
                company.getCompanyName(),
                company.getOwnerName(),
                company.getLogo(),
                company.getPassword(),

                // Billing Address
                company.getBillingAddress1(),
                company.getBillingAddress2(),
                company.getBillingAddress3(),
                company.getBillingPincode(),
                company.getBillingCity() != null ? company.getBillingCity().getCityId() : null,
                company.getBillingCity() != null ? company.getBillingCity().getCityName() : null,
                company.getBillingState() != null ? company.getBillingState().getStateId() : null,
                company.getBillingState() != null ? company.getBillingState().getStateName() : null,

                // Financial / Contact (CORRECT ORDER)
                company.getPanNumber(),
                company.getGstNumber(),
                company.getMobileNumber(),
                company.getAlternateMobileNumber(),

                company.getEmail(),
                company.getCinNumber(),


                company.getIndustry(),

                // Status
                company.getStatus(),
                company.getIsDeleted(),

                // Audit
                company.getCreatedAt(),
                company.getUpdatedAt(),
                company.getDeletedAt(),

                // Relations
                bankDetailsList,
                invoiceList
        );
    }


    private static InvoiceDto mapInvoiceToDto(InvoiceGenerator invoice, Integer companyId) {

        return new InvoiceDto(
                invoice.getInvoiceId(),
                invoice.getInvoicePrefix(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : null,
                invoice.getTerms(),
                invoice.getDueDate() != null ? invoice.getDueDate().toString() : null,
                invoice.getPaymentMode(),
                invoice.getNarration(),

                invoice.getLrNumber(),
                invoice.getTransport(),
                invoice.getCommissionerate(),
                invoice.getRange(),
                invoice.getDivision(),

                invoice.getItems() != null
                        ? invoice.getItems().stream()
                        .map(item -> mapInvoiceItemToDto(item))
                        .collect(Collectors.toList())
                        : List.of(),

                invoice.getOtherCharges() != null
                        ? invoice.getOtherCharges().stream()
                        .map(oc -> new OtherChargeDto(
                                oc.getOtherChargeId(),
                                oc.getLabel(),
                                oc.getValue(),
                                oc.getInvoice().getInvoiceId()))
                        .collect(Collectors.toList())
                        : List.of(),

                invoice.getTotalTaxableAmount(),
                invoice.getTotalCgst(),
                invoice.getTotalSgst(),
                invoice.getTotalIgst(),
                invoice.getRoundOff(),
                invoice.getGrandTotal(),

                invoice.getStatus(),
                invoice.getIsDeleted(),
                invoice.getIsRcm(),

                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                invoice.getDeletedAt(),

                companyId,

                invoice.getCustomer() != null
                        ? invoice.getCustomer().getCustomerId()
                        : null,

                invoice.getCustomer() != null
                        ? mapCustomerToDto(invoice.getCustomer())
                        : null
        );
    }


    private static InvoiceItemDTO mapInvoiceItemToDto(InvoiceItem item) {
        return new InvoiceItemDTO(
                item.getProduct() != null ? item.getProduct().getProductId() : null,
                item.getQuantity(),
                item.getRate(),

                item.getRoyalty(),
                item.getDmf(),
                item.getNmet(),

                item.getRoyaltyAmount(),
                item.getDmfAmount(),
                item.getNmetAmount(),

                item.getTaxableAmount(),
                item.getCgstPercent(),
                item.getSgstPercent(),
                item.getCgstAmount(),
                item.getSgstAmount(),
                item.getIgstPercent(),
                item.getIgstAmount(),

                item.getProduct() != null ? mapProductToDto(item.getProduct()) : null
        );
    }

    private static ProductDto mapProductToDto(Product product) {

        ProductDto dto = new ProductDto();

        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setType(product.getType());
        dto.setHsnCode(product.getHsnCode());
        dto.setSacCode(product.getSacCode());
        dto.setUnit(product.getUnit());
        dto.setTaxPreference(product.getTaxPreference());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setGstPercent(product.getGstPercent());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus());
        dto.setIsDeleted(product.getIsDeleted());

        // ✅ Mining fields
        dto.setMiningProduct(product.getMiningProduct());
        dto.setRoyalty(product.getRoyalty());
        dto.setDmf(product.getDmf());
        dto.setNmet(product.getNmet());

        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setDeletedAt(product.getDeletedAt());

        if (product.getCompanyRegistration() != null) {
            dto.setCompanyId(product.getCompanyRegistration().getCompanyId());
        }

        return dto;
    }


    private static CustomerDto mapCustomerToDto(Customer customer) {
        return new CustomerDto(
                customer.getCustomerId(),
                customer.getVid(),
                customer.getCustomerType(),
                customer.getSalutation(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getCustomerCompanyName(),
                customer.getDisplayName(),
                customer.getEmail(),
                customer.getWorkPhone(),
                customer.getMobileNumber(),
                customer.getGstNumber(),
                customer.getPan(),
                customer.getTerms(),

                customer.getBillingAddress() != null ? customer.getBillingAddress().getAttention() : null,
                customer.getBillingAddress() != null ? customer.getBillingAddress().getAddressLine1() : null,
                customer.getBillingAddress() != null ? customer.getBillingAddress().getAddressLine2() : null,
                customer.getBillingAddress() != null ? customer.getBillingAddress().getPincode() : null,

                customer.getBillingCity() != null ? customer.getBillingCity().getCityId() : null,
                customer.getBillingCity() != null ? customer.getBillingCity().getCityName() : null,
                customer.getBillingState() != null ? customer.getBillingState().getStateId() : null,
                customer.getBillingState() != null ? customer.getBillingState().getStateName() : null,

                customer.getPlaceOfSupply() != null ? customer.getPlaceOfSupply().getStateId() : null,
                customer.getPlaceOfSupply() != null ? customer.getPlaceOfSupply().getStateName() : null,

                customer.getShippingAddress() != null ? customer.getShippingAddress().getAttention() : null,
                customer.getShippingAddress() != null ? customer.getShippingAddress().getAddressLine1() : null,
                customer.getShippingAddress() != null ? customer.getShippingAddress().getAddressLine2() : null,
                customer.getShippingAddress() != null ? customer.getShippingAddress().getPincode() : null,

                customer.getShippingCity() != null ? customer.getShippingCity().getCityId() : null,
                customer.getShippingCity() != null ? customer.getShippingCity().getCityName() : null,
                customer.getShippingState() != null ? customer.getShippingState().getStateId() : null,
                customer.getShippingState() != null ? customer.getShippingState().getStateName() : null,

                customer.isSameAsBillingAddress(),

                customer.getStatus(),
                customer.getIsDeleted(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getDeletedAt(),

                customer.getContactPersons() != null ?
                        customer.getContactPersons().stream()
                                .map(cp -> new ContactPersonDTO(
                                        cp.getContactPersonId(),
                                        cp.getSalutation(),
                                        cp.getFirstName(),
                                        cp.getLastName(),
                                        cp.getEmail(),
                                        cp.getWorkPhone(),
                                        cp.getMobileNumber(),
                                        cp.getStatus(),
                                        cp.getIsDeleted(),
                                        cp.getCreatedAt(),
                                        cp.getUpdatedAt(),
                                        cp.getDeletedAt()
                                )).collect(Collectors.toList())
                        : List.of(),

                // Invoices
                customer.getInvoices() != null
                        ? customer.getInvoices().stream()
                        .map(inv -> new InvoiceDto(
                                inv.getInvoiceId(),
                                inv.getInvoicePrefix(),
                                inv.getInvoiceNumber(),
                                inv.getInvoiceDate(),
                                inv.getTerms(),
                                inv.getDueDate(),
                                inv.getPaymentMode(),
                                inv.getNarration(),
                                inv.getLrNumber(),
                                inv.getTransport(),
                                inv.getCommissionerate(),
                                inv.getRange(),
                                inv.getDivision(),
                                inv.getItems() != null
                                        ? inv.getItems().stream()
                                        .map(item -> new InvoiceItemDTO(
                                                item.getProduct() != null ? item.getProduct().getProductId() : null,
                                                item.getQuantity(),
                                                item.getRate(),

                                                // 🔹 Mining Fields
                                                item.getRoyalty(),
                                                item.getDmf(),
                                                item.getNmet(),

                                                item.getRoyaltyAmount(),
                                                item.getDmfAmount(),
                                                item.getNmetAmount(),

                                                // 🔹 Tax Fields
                                                item.getTaxableAmount(),
                                                item.getCgstPercent(),
                                                item.getSgstPercent(),
                                                item.getCgstAmount(),
                                                item.getSgstAmount(),
                                                item.getIgstPercent(),
                                                item.getIgstAmount(),

                                                item.getProduct() != null
                                                        ? new ProductDto(
                                                        item.getProduct().getProductId(),
                                                        item.getProduct().getProductName(),
                                                        item.getProduct().getType(),
                                                        item.getProduct().getHsnCode(),
                                                        item.getProduct().getSacCode(),
                                                        item.getProduct().getUnit(),
                                                        item.getProduct().getTaxPreference(),
                                                        item.getProduct().getSellingPrice(),
                                                        item.getProduct().getQuantity(),
                                                        item.getProduct().getRate(),
                                                        item.getProduct().getTaxValue(),
                                                        item.getProduct().getGstPercent(),
                                                        item.getProduct().getCgstAmount(),
                                                        item.getProduct().getSgstAmount(),
                                                        item.getProduct().getIgstAmount(),
                                                        item.getProduct().getNetAmount(),
                                                        item.getProduct().getDescription(),

                                                        // Mining product fields
                                                        item.getProduct().getMiningProduct(),
                                                        item.getProduct().getRoyalty(),
                                                        item.getProduct().getDmf(),
                                                        item.getProduct().getNmet(),

                                                        item.getProduct().getStatus(),
                                                        item.getProduct().getIsDeleted(),
                                                        item.getProduct().getCreatedAt(),
                                                        item.getProduct().getUpdatedAt(),
                                                        item.getProduct().getDeletedAt(),
                                                        item.getProduct().getCompanyRegistration() != null
                                                                ? item.getProduct().getCompanyRegistration().getCompanyId()
                                                                : null
                                                )
                                                        : null
                                        ))
                                        .collect(Collectors.toList())
                                        : List.of(),
                                           // otherChargeDtos
                                        inv.getOtherCharges() != null
                                        ? inv.getOtherCharges().stream().map(oc -> new OtherChargeDto(
                                        oc.getOtherChargeId(),
                                        oc.getLabel(),
                                        oc.getValue(),
                                        oc.getInvoice() != null ? oc.getInvoice().getInvoiceId() : null
                                       )).collect(Collectors.toList())
                                        : List.of(),

                                inv.getTotalTaxableAmount(),
                                inv.getTotalCgst(),
                                inv.getTotalSgst(),
                                inv.getTotalIgst(),
                                inv.getRoundOff(),
                                inv.getGrandTotal(),
                                inv.getStatus(),
                                inv.getIsDeleted(),
                                inv.getIsRcm(),
                                inv.getCreatedAt(),
                                inv.getUpdatedAt(),
                                inv.getDeletedAt(),
                                inv.getCompanyRegistration() != null ? inv.getCompanyRegistration().getCompanyId() : null,
                                inv.getCustomer() != null ? inv.getCustomer().getCustomerId() : null,
                                null // avoid recursive CustomerDto
                        )).collect(Collectors.toList())
                        : List.of(),



                customer.getCompanyRegistration() != null ? customer.getCompanyRegistration().getCompanyId() : null
        );
    }




    public List<CompanyDto> companiesToDtos(List<CompanyRegistration> companies) {
        return companies.stream()
                .map(AppUtils::companyToDto)
                .collect(Collectors.toList());
    }





    //---------------------------------------------------BANK DETAILS UTILS ----------------------------------------------------


    public BankDetailsDto bankToDto(BankDetails bank) {
        return new BankDetailsDto(
                bank.getBankId(),
                bank.getBankName(),
                bank.getIfscCode(),
                bank.getBranch(),
                bank.getAccountNumber(),
                bank.getAccHolderName(),
                bank.getBankAddress(),
                bank.getStatus(),
                bank.getIsDeleted(),
                bank.getCreatedAt(),
                bank.getUpdatedAt(),
                bank.getDeletedAt(),
                bank.getCompanyRegistration() != null ? bank.getCompanyRegistration().getCompanyId() : null
        );
    }


    public List<BankDetailsDto> banksToDtos(List<BankDetails> banks) {
        List<BankDetailsDto> dtos = new ArrayList<>();

        for (BankDetails b : banks) {
            BankDetailsDto dto = new BankDetailsDto();

            dto.setBankId(b.getBankId());
            dto.setBankName(b.getBankName());
            dto.setIfscCode(b.getIfscCode());
            dto.setBranch(b.getBranch());
            dto.setAccountNumber(b.getAccountNumber());
            dto.setStatus(b.getStatus());
            dto.setIsDeleted(b.getIsDeleted());
            dto.setCreatedAt(b.getCreatedAt());
            dto.setUpdatedAt(b.getUpdatedAt());
            dto.setDeletedAt(b.getDeletedAt());

            // Safely set companyId if companyRegistration is not null
            if (b.getCompanyRegistration() != null) {
                dto.setCompanyId(b.getCompanyRegistration().getCompanyId());
            }

            dtos.add(dto);
        }

        return dtos;
    }


    //---------------------------------------------------INVOICE DETAILS UTILS----------------------------------------------------


    public List<InvoiceDto> invoicesToDtos(List<InvoiceGenerator> invoices) {
        if (invoices == null || invoices.isEmpty()) return new ArrayList<>();

        List<InvoiceDto> dtoList = new ArrayList<>();

        for (InvoiceGenerator invoice : invoices) {
            InvoiceDto dto = new InvoiceDto();
            dto.setInvoiceId(invoice.getInvoiceId());
            dto.setInvoicePrefix(invoice.getInvoicePrefix());
            dto.setInvoiceNumber(invoice.getInvoiceNumber());

            // Format invoiceDate and dueDate to string (ISO_LOCAL_DATE)
            dto.setInvoiceDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : null);
            dto.setDueDate(invoice.getDueDate() != null ? invoice.getDueDate().toString() : null);

            dto.setTerms(invoice.getTerms());
            dto.setPaymentMode(invoice.getPaymentMode());
            dto.setNarration(invoice.getNarration());
            invoice.setIsRcm(invoice.getIsRcm() != null ? invoice.getIsRcm() : false); // Save RCM flag


            // Map invoice items
            if (invoice.getItems() != null) {
                List<InvoiceItemDTO> itemDTOs = invoice.getItems().stream().map(item -> {
                    InvoiceItemDTO itemDto = new InvoiceItemDTO();
                    itemDto.setProductId(item.getProduct().getProductId());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setRate(item.getRate());
                    itemDto.setTaxableAmount(item.getTaxableAmount());
                    itemDto.setCgstPercent(item.getCgstPercent());
                    itemDto.setSgstPercent(item.getSgstPercent());
                    itemDto.setCgstAmount(item.getCgstAmount());
                    itemDto.setSgstAmount(item.getSgstAmount());
                    itemDto.setIgstPercent(item.getIgstPercent());
                    itemDto.setIgstAmount(item.getIgstAmount());
                    return itemDto;
                }).toList();
                dto.setItems(itemDTOs);
            }

            dto.setTotalTaxableAmount(invoice.getTotalTaxableAmount());
            dto.setTotalCgst(invoice.getTotalCgst());
            dto.setTotalSgst(invoice.getTotalSgst());
            dto.setRoundOff(invoice.getRoundOff());
            dto.setGrandTotal(invoice.getGrandTotal());

            dto.setStatus(invoice.getStatus());
            dto.setIsDeleted(invoice.getIsDeleted());

            dto.setCreatedAt(invoice.getCreatedAt());
            dto.setUpdatedAt(invoice.getUpdatedAt());
            dto.setDeletedAt(invoice.getDeletedAt());

            // Set Company ID
            dto.setCompanyId(invoice.getCompanyRegistration() != null ? invoice.getCompanyRegistration().getCompanyId() : null);

            // Set Customer ID
            dto.setCustomerId(invoice.getCustomer() != null ? invoice.getCustomer().getCustomerId() : null);

            dtoList.add(dto);
        }

        return dtoList;
    }

    public InvoiceDto invoiceToDto(InvoiceGenerator invoice) {
        if (invoice == null) return null;

        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoicePrefix(invoice.getInvoicePrefix());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentMode(invoice.getPaymentMode());
        dto.setNarration(invoice.getNarration());

        if (invoice.getOtherCharges() != null && !invoice.getOtherCharges().isEmpty()) {
            dto.setOtherCharge(
                    invoice.getOtherCharges().stream()
                            .map(oc -> new OtherChargeDto(
                                    oc.getOtherChargeId(),
                                    oc.getLabel(),
                                    oc.getValue(),
                                    oc.getInvoice().getInvoiceId()
                            ))
                            .collect(Collectors.toList())
            );
        }


        dto.setTotalTaxableAmount(invoice.getTotalTaxableAmount());
        dto.setTotalCgst(invoice.getTotalCgst());
        dto.setTotalSgst(invoice.getTotalSgst());
        dto.setTotalIgst(invoice.getTotalIgst());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setRoundOff(invoice.getRoundOff());
        dto.setStatus(invoice.getStatus());
        dto.setIsDeleted(invoice.getIsDeleted());
        dto.setIsRcm(invoice.getIsRcm());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setDeletedAt(invoice.getDeletedAt());
        dto.setTerms(invoice.getTerms());

        // Company
        if (invoice.getCompanyRegistration() != null) {
            dto.setCompanyId(invoice.getCompanyRegistration().getCompanyId());
        }

        // Customer details
        if (invoice.getCustomer() != null) {
            Customer customer = invoice.getCustomer();
            dto.setCustomerId(customer.getCustomerId());

            CustomerDto customerDto = new CustomerDto();
            customerDto.setCustomerId(customer.getCustomerId());
            customerDto.setVid(customer.getVid());
            customerDto.setCustomerType(customer.getCustomerType());
            customerDto.setSalutation(customer.getSalutation());
            customerDto.setFirstName(customer.getFirstName());
            customerDto.setLastName(customer.getLastName());
            customerDto.setCustomerCompanyName(customer.getCustomerCompanyName());
            customerDto.setDisplayName(customer.getDisplayName());
            customerDto.setEmail(customer.getEmail());
            customerDto.setWorkPhone(customer.getWorkPhone());
            customerDto.setMobileNumber(customer.getMobileNumber());
            customerDto.setGstNumber(customer.getGstNumber());
            customerDto.setPan(customer.getPan());
            customerDto.setTerms(customer.getTerms());

            // Billing Address
            if (customer.getBillingAddress() != null) {
                customerDto.setBillingAttention(customer.getBillingAddress().getAttention());
                customerDto.setBillingAddressLine1(customer.getBillingAddress().getAddressLine1());
                customerDto.setBillingAddressLine2(customer.getBillingAddress().getAddressLine2());
                customerDto.setBillingPincode(customer.getBillingAddress().getPincode());
            }

            // Shipping Address
            if (customer.getShippingAddress() != null) {
                customerDto.setShippingAttention(customer.getShippingAddress().getAttention());
                customerDto.setShippingAddressLine1(customer.getShippingAddress().getAddressLine1());
                customerDto.setShippingAddressLine2(customer.getShippingAddress().getAddressLine2());
                customerDto.setShippingPincode(customer.getShippingAddress().getPincode());
            }

            // Cities and States
            if (customer.getBillingCity() != null) {
                customerDto.setBillingCityId(customer.getBillingCity().getCityId());
                customerDto.setBillingCityName(customer.getBillingCity().getCityName());
            }

            if (customer.getBillingState() != null) {
                customerDto.setBillingStateId(customer.getBillingState().getStateId());
                customerDto.setBillingStateName(customer.getBillingState().getStateName());
            }

            if (customer.getShippingCity() != null) {
                customerDto.setShippingCityId(customer.getShippingCity().getCityId());
                customerDto.setShippingCityName(customer.getShippingCity().getCityName());
            }

            if (customer.getShippingState() != null) {
                customerDto.setShippingStateId(customer.getShippingState().getStateId());
                customerDto.setShippingStateName(customer.getShippingState().getStateName());
            }

            if (customer.getPlaceOfSupply() != null) {
                customerDto.setPlaceOfSupplyStateId(customer.getPlaceOfSupply().getStateId());
                customerDto.setPlaceOfSupplyStateName(customer.getPlaceOfSupply().getStateName());
            }

            customerDto.setSameAsBillingAddress(customer.isSameAsBillingAddress());
            customerDto.setStatus(customer.getStatus());
            customerDto.setIsDeleted(customer.getIsDeleted());
            customerDto.setCreatedAt(customer.getCreatedAt());
            customerDto.setUpdatedAt(customer.getUpdatedAt());
            customerDto.setDeletedAt(customer.getDeletedAt());
            customerDto.setCompanyId(customer.getCompanyRegistration().getCompanyId());

            // Contact Persons
            if (customer.getContactPersons() != null && !customer.getContactPersons().isEmpty()) {
                customerDto.setContactPersons(
                        customer.getContactPersons().stream()
                                .map(cp -> new ContactPersonDTO(
                                        cp.getContactPersonId(),
                                        cp.getSalutation(),
                                        cp.getFirstName(),
                                        cp.getLastName(),
                                        cp.getEmail(),
                                        cp.getWorkPhone(),
                                        cp.getMobileNumber(),
                                        cp.getStatus(),
                                        cp.getIsDeleted(),
                                        cp.getCreatedAt(),
                                        cp.getUpdatedAt(),
                                        cp.getDeletedAt()
                                ))
                                .collect(Collectors.toList())
                );
            }

            dto.setCustomer(customerDto); // Set full customer details
        }

        // Invoice Items with Product Details
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            List<InvoiceItemDTO> invoiceItemDtos = invoice.getItems().stream().map(item -> {
                InvoiceItemDTO itemDto = new InvoiceItemDTO();

                itemDto.setProductId(item.getProduct().getProductId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setRate(item.getRate());
                itemDto.setTaxableAmount(item.getTaxableAmount());
                itemDto.setCgstAmount(item.getCgstAmount());
                itemDto.setSgstAmount(item.getSgstAmount());

                // Product DTO
                Product product = item.getProduct();
                ProductDto productDto = new ProductDto();

                productDto.setProductId(product.getProductId());
                productDto.setProductName(product.getProductName());
                productDto.setType(product.getType());
                productDto.setHsnCode(product.getHsnCode());
                productDto.setSacCode(product.getSacCode());
                productDto.setUnit(product.getUnit());
                productDto.setTaxPreference(product.getTaxPreference());
                productDto.setSellingPrice(product.getSellingPrice());
                productDto.setQuantity(product.getQuantity());
                productDto.setTaxValue(product.getTaxValue());
                productDto.setGstPercent(product.getGstPercent());
                productDto.setCgstAmount(product.getCgstAmount());
                productDto.setSgstAmount(product.getSgstAmount());
                productDto.setNetAmount(product.getNetAmount());
                productDto.setDescription(product.getDescription());
                productDto.setMiningProduct(product.getMiningProduct());
                productDto.setRoyalty(product.getRoyalty());
                productDto.setDmf(product.getDmf());
                productDto.setNmet(product.getNmet());

                productDto.setStatus(product.getStatus());
                productDto.setIsDeleted(product.getIsDeleted());
                productDto.setCreatedAt(product.getCreatedAt());
                productDto.setUpdatedAt(product.getUpdatedAt());
                productDto.setDeletedAt(product.getDeletedAt());
                productDto.setCompanyId(product.getCompanyRegistration().getCompanyId());

                itemDto.setProduct(productDto);
                return itemDto;
            }).collect(Collectors.toList());

            dto.setItems(invoiceItemDtos);
        }

        return dto;
    }




    //---------------------------------------------------PRODUCT DETAILS UTILS----------------------------------------------------

    public ProductDto productToDto(Product product) {

        ProductDto dto = new ProductDto();

        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setType(product.getType());
        dto.setHsnCode(product.getHsnCode());
        dto.setSacCode(product.getSacCode());
        dto.setUnit(product.getUnit());
        dto.setTaxPreference(product.getTaxPreference());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setQuantity(product.getQuantity());
        dto.setRate(product.getRate());
        dto.setTaxValue(product.getTaxValue());
        dto.setGstPercent(product.getGstPercent());
        dto.setCgstAmount(product.getCgstAmount());
        dto.setSgstAmount(product.getSgstAmount());
        dto.setIgstAmount(product.getIgstAmount());
        dto.setNetAmount(product.getNetAmount());
        dto.setDescription(product.getDescription());

        // ✅ Boolean fields
        dto.setStatus(product.getStatus());
        dto.setIsDeleted(product.getIsDeleted());

        // ✅ Mining fields (Double)
        dto.setMiningProduct(product.getMiningProduct());
        dto.setRoyalty(product.getRoyalty());
        dto.setDmf(product.getDmf());
        dto.setNmet(product.getNmet());

        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setDeletedAt(product.getDeletedAt());

        if (product.getCompanyRegistration() != null) {
            dto.setCompanyId(product.getCompanyRegistration().getCompanyId());
        }

        return dto;
    }



    public List<ProductDto> productsToDtos(List<Product> products) {

        List<ProductDto> dtos = new ArrayList<>();

        for (Product product : products) {
            ProductDto dto = new ProductDto();

            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setType(product.getType());
            dto.setHsnCode(product.getHsnCode());
            dto.setSacCode(product.getSacCode());
            dto.setUnit(product.getUnit());
            dto.setTaxPreference(product.getTaxPreference());
            dto.setSellingPrice(product.getSellingPrice());
            dto.setQuantity(product.getQuantity());
            dto.setRate(product.getRate());
            dto.setTaxValue(product.getTaxValue());
            dto.setGstPercent(product.getGstPercent());
            dto.setCgstAmount(product.getCgstAmount());
            dto.setSgstAmount(product.getSgstAmount());
            dto.setIgstAmount(product.getIgstAmount());
            dto.setNetAmount(product.getNetAmount());
            dto.setDescription(product.getDescription());

            // ✅ Add the missing fields
            dto.setMiningProduct(product.getMiningProduct());
            dto.setRoyalty(product.getRoyalty());
            dto.setDmf(product.getDmf());
            dto.setNmet(product.getNmet());

            dto.setStatus(product.getStatus());
            dto.setIsDeleted(product.getIsDeleted());
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());
            dto.setDeletedAt(product.getDeletedAt());

            // ✅ Add companyId safely
            dto.setCompanyId(
                    product.getCompanyRegistration() != null
                            ? product.getCompanyRegistration().getCompanyId()
                            : null
            );

            dtos.add(dto);
        }

        return dtos;
    }


    //---------------------------------------------------CUSTOMER Details Utils----------------------------------------------------


        // Convert CustomerDto to Customer entity
        public void updateCustomerFromDto(CustomerDto customerDto, Customer customer,
                                          Address billingCity, Address billingState,
                                          Address shippingCity, Address shippingState) {
            customer.setSalutation(customerDto.getSalutation());
            customer.setFirstName(customerDto.getFirstName());
            customer.setLastName(customerDto.getLastName());
            customer.setCustomerCompanyName(customerDto.getCustomerCompanyName());
            customer.setDisplayName(customerDto.getDisplayName());
            customer.setEmail(customerDto.getEmail());
            customer.setWorkPhone(customerDto.getWorkPhone());
            customer.setMobileNumber(customerDto.getMobileNumber());
            customer.setGstNumber(customerDto.getGstNumber());
            customer.setPan(customerDto.getPan());

            // Set Billing Address
            customer.setBillingAddress(billingCity); // Assuming billingCity has address information

            // Set Shipping Address if not same as Billing
            if (!customerDto.isSameAsBillingAddress()) {
                customer.setShippingAddress(shippingCity); // Assuming shippingCity has address information
            } else {
                customer.setShippingAddress(billingCity); // Same as Billing Address
            }
        }

        // Convert a list of ContactPersonDTOs to list of ContactPerson entities
        public List<ContactPerson> toEntityContactPersons(List<ContactPersonDTO> contactPersonDTOs, Customer customer) {
            return contactPersonDTOs.stream().map(dto -> {
                ContactPerson contactPerson = new ContactPerson();
                contactPerson.setSalutation(dto.getSalutation());
                contactPerson.setFirstName(dto.getFirstName());
                contactPerson.setLastName(dto.getLastName());
                contactPerson.setEmail(dto.getEmail());
                contactPerson.setWorkPhone(dto.getWorkPhone());
                contactPerson.setMobileNumber(dto.getMobileNumber());
                contactPerson.setStatus(dto.getStatus() != null ? dto.getStatus() : true);
                contactPerson.setIsDeleted(false);
                contactPerson.setCreatedAt(LocalDateTime.now());
                contactPerson.setUpdatedAt(LocalDateTime.now());
                contactPerson.setDeletedAt(null);
                contactPerson.setCustomer(customer);
                return contactPerson;
            }).collect(Collectors.toList());
        }

    // Convert Customer to CustomerDto
    public CustomerDto customerToDto(Customer customer) {
        CustomerDto customerDto = new CustomerDto();

        customerDto.setCustomerId(customer.getCustomerId());
        customerDto.setVid(customer.getVid());
        customerDto.setCustomerType(customer.getCustomerType());
        customerDto.setSalutation(customer.getSalutation());
        customerDto.setFirstName(customer.getFirstName());
        customerDto.setLastName(customer.getLastName());
        customerDto.setDisplayName(customer.getDisplayName());
        customerDto.setEmail(customer.getEmail());
        customerDto.setWorkPhone(customer.getWorkPhone());
        customerDto.setMobileNumber(customer.getMobileNumber());
        customerDto.setGstNumber(customer.getGstNumber());
        customerDto.setPan(customer.getPan());
        customerDto.setTerms(customer.getTerms());
        customerDto.setStatus(customer.getStatus());
        customerDto.setIsDeleted(customer.getIsDeleted());
        customerDto.setCreatedAt(customer.getCreatedAt());
        customerDto.setUpdatedAt(customer.getUpdatedAt());
        customerDto.setDeletedAt(customer.getDeletedAt());
        customerDto.setCustomerCompanyName(customer.getCustomerCompanyName());

        if (customer.getCompanyRegistration() != null) {
            customerDto.setCompanyId(customer.getCompanyRegistration().getCompanyId());
        }
        if (customer.getPlaceOfSupply() != null) {
            customerDto.setPlaceOfSupplyStateId(customer.getPlaceOfSupply().getStateId());
            customerDto.setPlaceOfSupplyStateName(customer.getPlaceOfSupply().getStateName());
        }


        // Billing Address
        if (customer.getBillingAddress() != null) {
            Address billing = customer.getBillingAddress();

            customerDto.setBillingAttention(billing.getAttention());
            customerDto.setBillingAddressLine1(billing.getAddressLine1());
            customerDto.setBillingAddressLine2(billing.getAddressLine2());
            customerDto.setBillingPincode(billing.getPincode());

            // City and State for Billing Address
            if (customer.getBillingCity() != null) {
                customerDto.setBillingCityId(customer.getBillingCity().getCityId());
                customerDto.setBillingCityName(customer.getBillingCity().getCityName());
            }

            if (customer.getBillingState() != null) {
                customerDto.setBillingStateId(customer.getBillingState().getStateId());
                customerDto.setBillingStateName(customer.getBillingState().getStateName());
            }
        }

        // Shipping Address
        if (customer.getShippingAddress() != null) {
            Address shipping = customer.getShippingAddress();

            customerDto.setShippingAttention(shipping.getAttention());
            customerDto.setShippingAddressLine1(shipping.getAddressLine1());
            customerDto.setShippingAddressLine2(shipping.getAddressLine2());
            customerDto.setShippingPincode(shipping.getPincode());

            // City and State for Shipping Address
            if (customer.getShippingCity() != null) {
                customerDto.setShippingCityId(customer.getShippingCity().getCityId());
                customerDto.setShippingCityName(customer.getShippingCity().getCityName());
            }

            if (customer.getShippingState() != null) {
                customerDto.setShippingStateId(customer.getShippingState().getStateId());
                customerDto.setShippingStateName(customer.getShippingState().getStateName());
            }
        }

        customerDto.setSameAsBillingAddress(customer.isSameAsBillingAddress());

        // Contact Persons
        if (customer.getContactPersons() != null && !customer.getContactPersons().isEmpty()) {
            customerDto.setContactPersons(
                    customer.getContactPersons().stream()
                            .map(contactPerson -> new ContactPersonDTO(
                                    contactPerson.getContactPersonId(),
                                    contactPerson.getSalutation(),
                                    contactPerson.getFirstName(),
                                    contactPerson.getLastName(),
                                    contactPerson.getEmail(),
                                    contactPerson.getWorkPhone(),
                                    contactPerson.getMobileNumber(),
                                    contactPerson.getStatus(),
                                    contactPerson.getIsDeleted(),
                                    contactPerson.getCreatedAt(),
                                    contactPerson.getUpdatedAt(),
                                    contactPerson.getDeletedAt()
                            ))
                            .collect(Collectors.toList())
            );
        }

        // Invoices
        // Invoices
        if (customer.getInvoices() != null && !customer.getInvoices().isEmpty()) {
            customerDto.setInvoices(
                    customer.getInvoices().stream()
                            .map(inv -> new InvoiceDto(
                                    inv.getInvoiceId(),
                                    inv.getInvoicePrefix(),
                                    inv.getInvoiceNumber(),
                                    inv.getInvoiceDate(),
                                    inv.getTerms(),
                                    inv.getDueDate(),
                                    inv.getPaymentMode(),
                                    inv.getNarration(),
                                    inv.getLrNumber(),
                                    inv.getTransport(),
                                    inv.getCommissionerate(),
                                    inv.getRange(),
                                    inv.getDivision(),
                                    inv.getItems() != null
                                            ? inv.getItems().stream()
                                            .map(item -> new InvoiceItemDTO(
                                                    item.getProduct() != null ? item.getProduct().getProductId() : null,
                                                    item.getQuantity(),
                                                    item.getRate(),

                                                    // 🔹 Mining Fields
                                                    item.getRoyalty(),
                                                    item.getDmf(),
                                                    item.getNmet(),

                                                    item.getRoyaltyAmount(),
                                                    item.getDmfAmount(),
                                                    item.getNmetAmount(),

                                                    // 🔹 Tax Fields
                                                    item.getTaxableAmount(),
                                                    item.getCgstPercent(),
                                                    item.getSgstPercent(),
                                                    item.getCgstAmount(),
                                                    item.getSgstAmount(),
                                                    item.getIgstPercent(),
                                                    item.getIgstAmount(),

                                                    item.getProduct() != null
                                                            ? productToDto(item.getProduct())
                                                            : null
                                            ))
                                            .collect(Collectors.toList())
                                            : List.of(),

                                    // otherChargeDtos (this is the part you wanted to add)
                                    inv.getOtherCharges() != null
                                            ? inv.getOtherCharges().stream()
                                            .map(oc -> new OtherChargeDto(
                                                    oc.getOtherChargeId(),
                                                    oc.getLabel(),
                                                    oc.getValue(),
                                                    oc.getInvoice() != null ? oc.getInvoice().getInvoiceId() : null
                                            )).collect(Collectors.toList())
                                            : List.of(),

                                    inv.getTotalTaxableAmount(),
                                    inv.getTotalCgst(),
                                    inv.getTotalSgst(),
                                    inv.getTotalIgst(),
                                    inv.getRoundOff(),
                                    inv.getGrandTotal(),
                                    inv.getStatus(),
                                    inv.getIsDeleted(),
                                    inv.getIsRcm(),
                                    inv.getCreatedAt(),
                                    inv.getUpdatedAt(),
                                    inv.getDeletedAt(),
                                    inv.getCompanyRegistration() != null ? inv.getCompanyRegistration().getCompanyId() : null,
                                    inv.getCustomer() != null ? inv.getCustomer().getCustomerId() : null,
                                    null // avoid recursive CustomerDto
                            ))
                            .collect(Collectors.toList())
            );
        }

        return customerDto;
    }



        // Convert Address entity to AddressDto
        public AddressDto toAddressDto(Address address) {
            if (address == null) {
                return null;
            }
            AddressDto addressDto = new AddressDto();
            addressDto.setAttention(address.getAttention());
            addressDto.setAddressLine1(address.getAddressLine1());
            addressDto.setAddressLine2(address.getAddressLine2());
            addressDto.setPincode(address.getPincode());
            return addressDto;
        }

        // Convert AddressDto to Address entity
        public Address toEntityAddress(AddressDto addressDto) {
            if (addressDto == null) {
                return null;
            }
            Address address = new Address();
            address.setAttention(addressDto.getAttention());
            address.setAddressLine1(addressDto.getAddressLine1());
            address.setAddressLine2(addressDto.getAddressLine2());
            address.setPincode(addressDto.getPincode());
            return address;
        }

        // Convert List<Customer> to List<CustomerDto>
        public List<CustomerDto> customersToDtos(List<Customer> customers) {
            return customers.stream().map(this::convertCustomerToDto)
                    .collect(Collectors.toList());
        }

    // Single customer to DTO conversion
    private CustomerDto convertCustomerToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();

        dto.setCustomerId(customer.getCustomerId());
        dto.setCustomerType(customer.getCustomerType());
        dto.setSalutation(customer.getSalutation());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
//        dto.setCustomerCompanyName(customer.getCustomerCompanyName());
        dto.setDisplayName(customer.getDisplayName());
        dto.setEmail(customer.getEmail());
        dto.setWorkPhone(customer.getWorkPhone());
        dto.setMobileNumber(customer.getMobileNumber());
        dto.setGstNumber(customer.getGstNumber());
        dto.setPan(customer.getPan());
        dto.setStatus(customer.getStatus());
        dto.setIsDeleted(customer.getIsDeleted());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        dto.setDeletedAt(customer.getDeletedAt());
        dto.setCompanyId(customer.getCompanyRegistration() != null ? customer.getCompanyRegistration().getCompanyId() : null);
        dto.setCustomerCompanyName(customer.getCustomerCompanyName());
        dto.setCompanyId(
                customer.getCompanyRegistration() != null
                        ? customer.getCompanyRegistration().getCompanyId()
                        : null
        );

        dto.setPlaceOfSupplyStateId(customer.getPlaceOfSupply()!=null ? customer.getPlaceOfSupply().getStateId():null);
        dto.setPlaceOfSupplyStateName(customer.getPlaceOfSupply()!=null ? customer.getPlaceOfSupply().getStateName():null);
        dto.setSameAsBillingAddress(customer.isSameAsBillingAddress());

        // Billing Address
        if (customer.getBillingAddress() != null) {
            dto.setBillingAttention(customer.getBillingAddress().getAttention());
            dto.setBillingAddressLine1(customer.getBillingAddress().getAddressLine1());
            dto.setBillingAddressLine2(customer.getBillingAddress().getAddressLine2());
            dto.setBillingPincode(customer.getBillingAddress().getPincode());
        }

        // Billing City and State
        if (customer.getBillingCity() != null) {
            dto.setBillingCityId(customer.getBillingCity().getCityId());
            dto.setBillingCityName(customer.getBillingCity().getCityName());
        }
        if (customer.getBillingState() != null) {
            dto.setBillingStateId(customer.getBillingState().getStateId());
            dto.setBillingStateName(customer.getBillingState().getStateName());
        }

        // Shipping Address
        if (customer.getShippingAddress() != null) {
            dto.setShippingAttention(customer.getShippingAddress().getAttention());
            dto.setShippingAddressLine1(customer.getShippingAddress().getAddressLine1());
            dto.setShippingAddressLine2(customer.getShippingAddress().getAddressLine2());
            dto.setShippingPincode(customer.getShippingAddress().getPincode());
        }

        // Shipping City and State
        if (customer.getShippingCity() != null) {
            dto.setShippingCityId(customer.getShippingCity().getCityId());
            dto.setShippingCityName(customer.getShippingCity().getCityName());
        }
        if (customer.getShippingState() != null) {
            dto.setShippingStateId(customer.getShippingState().getStateId());
            dto.setShippingStateName(customer.getShippingState().getStateName());
        }

        // Contact persons
        if (customer.getContactPersons() != null) {
            List<ContactPersonDTO> contactPersonDTOs = customer.getContactPersons().stream().map(contactPerson -> {
                ContactPersonDTO contactDto = new ContactPersonDTO();
                contactDto.setContactPersonId(contactPerson.getContactPersonId());
                contactDto.setSalutation(contactPerson.getSalutation());
                contactDto.setFirstName(contactPerson.getFirstName());
                contactDto.setLastName(contactPerson.getLastName());
                contactDto.setEmail(contactPerson.getEmail());
                contactDto.setWorkPhone(contactPerson.getWorkPhone());
                contactDto.setMobileNumber(contactPerson.getMobileNumber());
                contactDto.setStatus(contactPerson.getStatus() != null ? contactPerson.getStatus() : true);
                contactDto.setIsDeleted(false);
                contactDto.setCreatedAt(LocalDateTime.now());
                contactDto.setUpdatedAt(LocalDateTime.now());
                contactDto.setDeletedAt(null);
                return contactDto;
            }).collect(Collectors.toList());
            dto.setContactPersons(contactPersonDTOs);
        }

        return dto;
    }


    //---------------------------------------------------STATE UTILS----------------------------------------------------

    public StateDto stateToDto(State s) {
        return new StateDto(s.getStateId(), s.getStateName(), s.getStatus(), s.getIsDeleted(), s.getCreatedAt(), s.getUpdatedAt(), s.getDeletedAt());
    }

    public List<StateDto> statesToDtos(List<State> states) {

        List<StateDto> dtos = new ArrayList<>();

        for (State s : states) {
            dtos.add(new StateDto(s.getStateId(), s.getStateName(), s.getStatus(), s.getIsDeleted(), s.getCreatedAt(), s.getUpdatedAt(), s.getDeletedAt()));
        }
        return dtos;
    }

    public List<State> dtosToStates(List<StateDto> dtos) {
        java.lang.reflect.Type targetListType = new TypeToken<List<State>>() {
        }.getType();
        return modelMapper.map(dtos, targetListType);
    }

    public List<StateExportDTO> stateExportDTOStoStates(List<State> states) {
        List<StateExportDTO> dtos = new ArrayList<>();
        for (State s : states) {
            dtos.add(new StateExportDTO(
                    s.getStateId(), s.getStateName(), s.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }




    //---------------------------------------------------CITY UTILS-------------------------------------------------

    public CityDto cityToDto(City c) {
        return new CityDto(c.getCityId() != null ? c.getCityId() : null, c.getCityName() != null ? c.getCityName() : null,
                c.getState() != null ? c.getState().getStateId() : null, c.getState() != null ? c.getState().getStateName() : null,
                c.getStatus() != null ? c.getStatus() : null, c.getIsDeleted() != null ? c.getIsDeleted() : null, c.getCreatedAt() != null ? c.getCreatedAt() : null, c.getUpdatedAt() != null ? c.getUpdatedAt() : null, c.getDeletedAt() != null ? c.getDeletedAt() : null);
    }

    public List<CityDto> citiesToDtos(List<City> cities) {
        List<CityDto> dtos = new ArrayList<>();
        for (City c : cities) {
            dtos.add(new CityDto(c.getCityId() != null ? c.getCityId() : null, c.getCityName() != null ? c.getCityName() : null,
                    c.getState() != null ? c.getState().getStateId() : null, c.getState() != null ? c.getState().getStateName() : null,
                    c.getStatus() != null ? c.getStatus() : null, c.getIsDeleted() != null ? c.getIsDeleted() : null, c.getCreatedAt() != null ? c.getCreatedAt() : null, c.getUpdatedAt() != null ? c.getUpdatedAt() : null, c.getDeletedAt() != null ? c.getDeletedAt() : null));        }
        return dtos;
    }

    public List<CityExportDTO> cityExportDTOStoCities(List<City> cities) {
        List<CityExportDTO> dtos = new ArrayList<>();
        for (City c : cities) {
            dtos.add(new CityExportDTO(
                    c.getCityId(), c.getCityName(), c.getState().getStateName(), c.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }



}
