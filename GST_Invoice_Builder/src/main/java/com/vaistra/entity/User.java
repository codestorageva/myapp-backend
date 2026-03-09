package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;


    private String fullName;

    private String email;

    private String password;

    private String pwd;

    private LocalDate dob;

    private String mobNo;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String message;

//    private String profilePicture;

    @Column(length = 1024)
    private String jwtToken;

    private String lastActiveToken;  // Store the last active session token

    private Boolean isDeleted;

    private Boolean isLoggedOut;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Boolean activeStatus;

    @OneToOne(mappedBy = "user")
    private Confirmation confirmation;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.getRoleName()));

        Hibernate.initialize(this.role.getPermissions());

        this.role.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getPermissionName())));

        return authorities;

    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activeStatus;
    }


}