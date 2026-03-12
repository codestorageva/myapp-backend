package com.vaistra.controller;

import com.vaistra.dto.UserDTO;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.UserApproveDTO;
import com.vaistra.dto.update.UserUpdateDTO;
import com.vaistra.entity.User;
import com.vaistra.repository.ConfirmationRepository;
import com.vaistra.repository.UserRepository;
import com.vaistra.service.EmailService;
import com.vaistra.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/user")
public class UserController {
    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final UserService userService;

    private final EmailService emailService;

    private final UserRepository userRepository;

    private final ConfirmationRepository confirmationRepository;


    public UserController(UserService userService, EmailService emailService, UserRepository userRepository, ConfirmationRepository confirmationRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.confirmationRepository = confirmationRepository;
    }


    //---------------------------------------------------URL ENDPOINTS--------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> addUser(@Valid @RequestBody UserDTO userDto) {
        return new ResponseEntity<>(userService.addUser(userDto), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getUserByToken")
    public ResponseEntity<DataResponse> getUserByToken(@AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.getUserByToken(loggedInUser), HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("{userId}")
    public ResponseEntity<DataResponse> getUserById(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllUser(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                   @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                   @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
                                                   @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                   @RequestParam(value = "activeStatus", defaultValue = "true", required = false) String activeStatus,
                                                   @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted) {
        return new ResponseEntity<>(userService.getAllUsers(keyword, pageNumber, pageSize, sortBy, sortDirection, activeStatus, isDeleted), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('USER_UPDATE')")
    @PutMapping("{userId}")
    public ResponseEntity<MessageResponse> updateUser(@Valid @RequestBody UserUpdateDTO userDto, @PathVariable int userId, @AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.updateUser(userDto, userId, loggedInUser), HttpStatus.OK);
    }


/*    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("hardDelete/{userId}")
    public ResponseEntity<MessageResponse> hardDeleteUserById(@PathVariable int userId) {
        return new ResponseEntity<>(userService.hardDeleteUserById(userId), HttpStatus.OK);
    }*/

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('USER_DELETE')")
    @PutMapping("softDelete/{userId}")
    public ResponseEntity<MessageResponse> softDeleteById(@PathVariable int userId, @AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.softDeleteUser(userId, loggedInUser), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('USER_RESTORE')")
    @PutMapping("restore/{userId}")
    public ResponseEntity<MessageResponse> restoreUserById(@PathVariable int userId, @AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.restoreUser(userId, loggedInUser), HttpStatus.OK);
    }


    @GetMapping("/totalUser")
    public ResponseEntity<LongResponse> getTotalUser() {
        return new ResponseEntity<>(userService.getTotalUser(), HttpStatus.OK);
    }



}
