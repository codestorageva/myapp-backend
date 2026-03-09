package com.vaistra.controller;

import com.vaistra.config.jwt.JwtService;
import com.vaistra.dto.*;
import com.vaistra.dto.response.AuthenticationResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.response.PermissionMappedDTO;
import com.vaistra.entity.Confirmation;
import com.vaistra.entity.Permission;
import com.vaistra.entity.Role;
import com.vaistra.entity.User;
import com.vaistra.exception.InvalidArgumentException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.exception.UserUnauthorizedException;
import com.vaistra.repository.ConfirmationRepository;
import com.vaistra.repository.UserRepository;
import com.vaistra.service.EmailService;
import com.vaistra.service.OTPService;
import com.vaistra.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://local-invoice.com", allowCredentials = "true")
@RestController
@RequestMapping("/auth")

public class AuthController {
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final AuthenticationManager manager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ConfirmationRepository confirmationRepository;

    @Autowired
    private OTPService otpService;

    private String generatedOtp;

    @Autowired
    public AuthController(UserDetailsService userDetailsService, UserService userService, AuthenticationManager manager, JwtService jwtService, UserRepository userRepository, EmailService emailService, ConfirmationRepository confirmationRepository) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.manager = manager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.confirmationRepository = confirmationRepository;
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {

        this.doAuthenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.jwtService.generateToken(userDetails);

        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername());

        if (user.getRole().getIsDeleted() || user.getIsDeleted()) {
            userService.logout(user);
            throw new UserUnauthorizedException("Either the role or the user is currently not available. Contact Vaistra Technologies Pvt. Ltd. authorized personnel.");
        }

        user.setIsLoggedOut(false);
        user.setJwtToken(token);
        userRepository.save(user);
        Role role = user.getRole();
        List<Permission> permissions = role.getPermissions();

        List<PermissionMappedDTO> permissionDTOList = permissions.stream()
                .map(permission -> PermissionMappedDTO.builder()
                        .permissionName(permission.getPermissionName())
                        .permissionId(permission.getPermissionId())
                        .build())
                .collect(Collectors.toList());


        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .authToken("Bearer " + token)
                .email(user.getUsername())
                .fullName(user.getFullName())
                .roleId(user.getRole().getRoleId())
                .roleName(user.getRole().getRoleName())
                .mobNo(user.getMobNo())
                .permissionList(permissionDTOList)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.logout(loggedInUser), HttpStatus.OK);
    }

    private void doAuthenticate(String username, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        try {
            manager.authenticate(authentication);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Password.");
        }
    }

    @PostMapping("/emailExist")
    public ResponseEntity<MessageResponse> existEmail(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        return new ResponseEntity<>(userService.checkEmailExist(forgotPasswordDTO), HttpStatus.OK);
    }

    @PostMapping("/forget-pwd")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        return new ResponseEntity<>(userService.forgotPassword(forgotPasswordDTO), HttpStatus.OK);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        return new ResponseEntity<>(userService.forgotPassword(forgotPasswordDTO), HttpStatus.OK);
    }


    @PostMapping("/chk-forget-otp")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpDTO verifyOtpDTO) {
        return new ResponseEntity<>(userService.verifyOtp(verifyOtpDTO), HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal User loggedInUser, @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        return new ResponseEntity<>(userService.changePassword(loggedInUser, changePasswordDTO), HttpStatus.OK);
    }

    @GetMapping("/validateToken")
    public ResponseEntity<MessageResponse> validateToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(userService.validateToken(HttpHeaders.AUTHORIZATION, loggedInUser), HttpStatus.OK);
    }

    @PostMapping("/mobile-otp-send")
    public MessageResponse sendOtp(@Valid @RequestBody SendMobileOTPDTO sendMobileOTPDTO) {
        generatedOtp = otpService.generateOtp();
        otpService.sendOtp(sendMobileOTPDTO.getMobNo(), generatedOtp);

        return new MessageResponse(true, HttpStatus.OK, "OTP sent successfully.");
    }

    @Transactional
    @PostMapping("/email-otp-send")
    public MessageResponse sendEmailOtp(@Valid @RequestBody SendEmailOTPDTO sendEmailOTPDTO) {
        String email = sendEmailOTPDTO.getEmail();

        Random random = new Random();
        String otp;

        // Generate a unique OTP (ensure it does not already exist in the database)
        do {
            otp = String.valueOf(random.nextInt(900000) + 100000);
        } while (confirmationRepository.existsByOtp(otp));

        System.out.println("Before delete operation: " + email);

        // **Delete all previous entries for this email**
        confirmationRepository.deleteByEmailIgnoreCase(email);

        System.out.println("After delete operation: " + email);

        // Send OTP to the user's email
        emailService.sendVerifyEmailOtp(email, otp);

        // Save the new OTP entry
        Confirmation confirmation = new Confirmation(email, otp);
        confirmationRepository.save(confirmation);

        return new MessageResponse(true, HttpStatus.OK, "OTP sent successfully. Please check your email.");
    }


    @PostMapping("/email-otp-verify")
    public MessageResponse verifyEmailOtp(@Valid @RequestBody VerifyEmailOTPDTO verifyEmailOTPDTO) {
        Confirmation confirmation = confirmationRepository.findByOtp(verifyEmailOTPDTO.getOtp());

        if (confirmation == null)
            throw new ResourceNotFoundException("Invalid OTP");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tokenCreationTime = confirmation.getCreatedAt();
        long minutesSinceCreation = Duration.between(tokenCreationTime, now).toMinutes();

        if (minutesSinceCreation > 30) {
            confirmationRepository.delete(confirmation);
            throw new ResourceNotFoundException("OTP Expired!");
        }

        confirmation.setIsVerified(true);
        confirmationRepository.save(confirmation);

        Confirmation conf = confirmationRepository.findByEmailIgnoreCase(verifyEmailOTPDTO.getEmail());
        if (conf == null)
            throw new ResourceNotFoundException("Invalid Email");

        confirmationRepository.delete(confirmation);

        emailService.sendEmailVerfication(verifyEmailOTPDTO.getEmail());

        return new MessageResponse(true, HttpStatus.OK, "Email OTP Verified Successfully.");
    }

    @PostMapping("/mobile-otp-verify")
    public MessageResponse verifyOtp(@Valid @RequestBody VerifyMobileOTPDTO verifyMobileOTPDTO) {
        Map<String, String> response = new HashMap<>();
        if (otpService.verifyOtp(generatedOtp, verifyMobileOTPDTO.getOtp())) {
            return new MessageResponse(true, HttpStatus.OK, "OTP Verified");
        } else {
            throw new InvalidArgumentException("Invalid OTP");
        }
    }

}
