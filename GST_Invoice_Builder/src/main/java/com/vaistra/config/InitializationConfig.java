package com.vaistra.config;


import com.vaistra.config.jwt.JwtService;
import com.vaistra.entity.Permission;
import com.vaistra.entity.Role;
import com.vaistra.entity.User;
import com.vaistra.repository.PermissionRepository;
import com.vaistra.repository.RoleRepository;
import com.vaistra.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class InitializationConfig {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;

    public InitializationConfig(UserRepository userRepository, RoleRepository roleRepository,JwtService jwtService, PasswordEncoder passwordEncoder, PermissionRepository permissionRepositoryl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepositoryl;
    }

    @PostConstruct
    public void initializePermission(){

//        if (!permissionRepository.existsByPermissionNameIgnoreCase("USER_WRITE")){
//            Permission rp = new Permission();
//
//            rp.setPermissionSuperGroup("AUTH_MANAGEMENT");
//            rp.setPermissionGroup("USER");
//            rp.setPermissionName("USER_WRITE");
//            rp.setCreatedAt(LocalDateTime.now());
//            rp.setGuardName("WEB");
//            rp.setIsDeleted(false);
//            rp.setUpdatedAt(LocalDateTime.now());
//            permissionRepository.save(rp);
//        }

        if (!permissionRepository.existsByPermissionNameIgnoreCase("USER_READ")){
            Permission permission = new Permission();

            permission.setPermissionSuperGroup("AUTH_MANAGEMENT");
            permission.setPermissionGroup("USER");
            permission.setPermissionName("USER_READ");
            permission.setCreatedAt(LocalDateTime.now());
            permission.setGuardName("WEB");
            permission.setIsDeleted(false);
            permission.setUpdatedAt(LocalDateTime.now());
            permissionRepository.save(permission);
        }

        if (!permissionRepository.existsByPermissionNameIgnoreCase("USER_UPDATE")){
            Permission up = new Permission();

            up.setPermissionSuperGroup("AUTH_MANAGEMENT");
            up.setPermissionGroup("USER");
            up.setPermissionName("USER_UPDATE");
            up.setCreatedAt(LocalDateTime.now());
            up.setGuardName("WEB");
            up.setIsDeleted(false);
            up.setUpdatedAt(LocalDateTime.now());
            permissionRepository.save(up);
        }

        if (!permissionRepository.existsByPermissionNameIgnoreCase("USER_DELETE")){
            Permission dp = new Permission();

            dp.setPermissionSuperGroup("AUTH_MANAGEMENT");
            dp.setPermissionGroup("USER");
            dp.setPermissionName("USER_DELETE");
            dp.setCreatedAt(LocalDateTime.now());
            dp.setGuardName("WEB");
            dp.setIsDeleted(false);
            dp.setUpdatedAt(LocalDateTime.now());
            permissionRepository.save(dp);
        }

        if (!permissionRepository.existsByPermissionNameIgnoreCase("USER_RESTORE")){
            Permission rp = new Permission();

            rp.setPermissionSuperGroup("AUTH_MANAGEMENT");
            rp.setPermissionGroup("USER");
            rp.setPermissionName("USER_RESTORE");
            rp.setCreatedAt(LocalDateTime.now());
            rp.setGuardName("WEB");
            rp.setIsDeleted(false);
            rp.setUpdatedAt(LocalDateTime.now());
            permissionRepository.save(rp);
        }


    }

    @PostConstruct
    public void initializeSuperAdmin() {
        if (!userRepository.existsByEmailIgnoreCase("superadmin@gmail.com")) {
            User user = new User();
            user.setFullName("SUPERADMIN");
            user.setEmail("superadmin@gmail.com");
            user.setPassword(passwordEncoder.encode("123456789"));
            user.setPwd("123456789");
            user.setDob(LocalDate.parse("2019-04-01"));
            user.setMobNo("+91 8758370456");
            user.setAddressLine1("Porbandar");
            user.setJwtToken(jwtService.generateToken(user));
            user.setActiveStatus(true);
            user.setIsDeleted(false);
            user.setIsLoggedOut(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            List<Permission> permissions = permissionRepository.findAll();

            permissions.forEach(permission ->
                    System.out.println("All Permissions" + permission.getPermissionName()));

            Role role = new Role();
            role.setRoleName("ROLE_SUPERADMIN");
            role.setGuardName("WEB");
            role.setPermissions(permissions);
            role.setIsDeleted(false);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            roleRepository.save(role);
            user.setRole(role);

            userRepository.save(user);


            Role userRole = new Role();
            userRole.setRoleName("ROLE_USER");
            userRole.setGuardName("WEB");
            userRole.setIsDeleted(false);
            userRole.setCreatedAt(LocalDateTime.now());
            userRole.setUpdatedAt(LocalDateTime.now());
            roleRepository.save(userRole);
        }

    }

}
