package com.vaistra.service.impl;

import com.vaistra.config.jwt.JwtService;
import com.vaistra.dto.*;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.UserUpdateDTO;
import com.vaistra.entity.Confirmation;
import com.vaistra.entity.Role;
import com.vaistra.entity.User;
import com.vaistra.exception.*;
import com.vaistra.repository.ConfirmationRepository;
import com.vaistra.repository.RoleRepository;
import com.vaistra.repository.UserRepository;
import com.vaistra.service.EmailService;
import com.vaistra.service.UserService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationRepository confirmationRepository;
    private final AppUtils appUtils;
    private final JwtService jwtService;
    //    private final AwsService awsService;
    private final EmailService emailService;
    private final JavaMailSender javaMailSender;
    private final RoleRepository roleRepository;
    private final EntityManager entityManager;
    private final RestTemplate template;
    private final HttpServletRequest request;  // Inject HttpServletRequest


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ConfirmationRepository confirmationRepository, AppUtils appUtils, JwtService jwtService, EmailService emailService, JavaMailSender javaMailSender, RoleRepository roleRepository, EntityManager entityManager, RestTemplate template, HttpServletRequest request) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationRepository = confirmationRepository;
        this.appUtils = appUtils;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.javaMailSender = javaMailSender;
        this.roleRepository = roleRepository;
        this.entityManager = entityManager;
        this.template = template;
        this.request = request;
    }

    //----------------------------------------------------SERVICE METHODS-----------------------------------------------
    @Override

    public UserRegisterResponse addUser(UserDTO UserDTO) {

        UserDTO.setEmail(UserDTO.getEmail().trim());

        if (userRepository.existsByEmailIgnoreCase(UserDTO.getEmail())) {
            throw new DuplicateEntryException("User with email '" + UserDTO.getEmail() + "' already exists!");
        }

        User user = new User();

        user.setFullName(UserDTO.getFullName());
        user.setEmail(UserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(UserDTO.getPassword()));
        user.setPwd(UserDTO.getPassword());
        user.setDob(UserDTO.getDob());
        user.setMobNo(UserDTO.getMobNo());
        user.setAddressLine1(UserDTO.getAddressLine1());
        user.setAddressLine2(UserDTO.getAddressLine2());
        user.setAddressLine3(UserDTO.getAddressLine3());
        user.setMessage(UserDTO.getMessage());
        user.setIsDeleted(false);
        user.setActiveStatus(true);
        user.setJwtToken(jwtService.generateToken(user));
        user.setIsLoggedOut(false);

        if (UserDTO.getRoleId() != null) {
            Role role = roleRepository.findById(UserDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role with ID '" + UserDTO.getRoleId() + "' not found!"));
            user.setRole(role);
        } else {
            Role role = roleRepository.findById(2)
                    .orElseThrow(() -> new ResourceNotFoundException("Role with ID '2' not found!"));
            user.setRole(role);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(null);

        userRepository.save(user);

        return new UserRegisterResponse(true, HttpStatus.OK, "User Registered Successfully.");
    }


    @Override
    public DataResponse getUserById(int id) {
        return new DataResponse(true, HttpStatus.OK, appUtils.userToDto(userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found!"))));
    }

    @Override
    public HttpResponse getAllUsers(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String activeStatus, String isDeleted) {
        Page<User> pageUser = null;
        List<UserDTO> users = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isActoveStatus = null;


        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        if (activeStatus.equalsIgnoreCase("true")) {
            isActoveStatus = Boolean.TRUE;
        } else if (activeStatus.equalsIgnoreCase("false")) {
            isActoveStatus = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate statusPredicate = criteriaBuilder.equal(root.get("activeStatus"), isActoveStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate userIdPredicate = criteriaBuilder.equal(root.get("userId"), intKeyword);
            Predicate userPredicate = null;

            if (keyword != null) {
                userPredicate = criteriaBuilder.or(

                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mobNo").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("role").get("roleName")).as(String.class), "%" + keyword.toLowerCase() + "%")
                );
            }

            Predicate combinedPredicate = null;

            if (isActoveStatus != null) {
                combinedPredicate = statusPredicate;

                if (softDeleted != null) {
                    if (intKeyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, userIdPredicate, deletedPredicate);
                    } else if (keyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, userPredicate, deletedPredicate);
                    } else {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate);
                    }
                }
            }

            // Create the query to retrieve a page of results
            criteriaQuery.select(root)
                    .where(criteriaBuilder.and(combinedPredicate));

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            // Fetch results for the current page
            List<User> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pageUser = new PageImpl<>(resultList, pageable, totalCount);

            users = appUtils.usersToDtos(resultList);

        } catch (NoResultException ignored) {

        }
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageUser.getNumber())
                .pageSize(pageUser.getSize())
                .totalElements(pageUser.getTotalElements())
                .totalPages(pageUser.getTotalPages())
                .isLastPage(pageUser.isLast())
                .data(users)
                .build();
    }

    @Override
    public MessageResponse updateUser(UserUpdateDTO userUpdateDTO, int id, User loggedInUser) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found!"));



        if (userUpdateDTO.getFullName() != null) {
            user.setFullName(userUpdateDTO.getFullName().trim());
        }


/*        if (userUpdateDTO.getEmail() != null) {
            User userWithSameEmail = userRepository.findByEmailIgnoreCase(userUpdateDTO.getEmail());
            if (userWithSameEmail != null && !userWithSameEmail.getUserId().equals(user.getUserId()))
                throw new DuplicateEntryException("User email '" + userUpdateDTO.getEmail() + "' already exist!");

            user.setEmail(userUpdateDTO.getEmail().trim());
        }*/

        if (userUpdateDTO.getDob() != null) {
            user.setDob(userUpdateDTO.getDob());
        }


        if (userUpdateDTO.getAddressLine1() != null) {
            user.setAddressLine1(userUpdateDTO.getAddressLine1());
        }

        if (userUpdateDTO.getAddressLine2() != null) {
            user.setAddressLine2(userUpdateDTO.getAddressLine2());
        }

        if (userUpdateDTO.getAddressLine3() != null) {
            user.setAddressLine3(userUpdateDTO.getAddressLine3());
        }

        if (userUpdateDTO.getRoleId() != null) {
            Role role = roleRepository.findById(userUpdateDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role with ID '" + userUpdateDTO.getRoleId() + "' not found!"));
            user.setRole(role);
            user.setJwtToken(null);
            user.setIsLoggedOut(true);
        }

        if (userUpdateDTO.getActiveStatus() != null) {
            user.setActiveStatus(userUpdateDTO.getActiveStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "User Updated Successfully.");
    }

    @Async
    @Override
    public MessageResponse softDeleteUser(int id, User loggedInUser) {
        // Retrieve Authorization header from request
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            throw new UserUnauthorizedException("Authorization header is missing!");
        }

        // Fetch user from DB
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID '" + id + "' not found!"));


        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setJwtToken(null);
        user.setIsLoggedOut(true);
        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "User with ID '" + id + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreUser(int id, User loggedInUser) {
        // Retrieve Authorization header from request
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            throw new UserUnauthorizedException("Authorization header is missing!");
        }

        // Fetch user from DB
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found!"));


        user.setIsDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "User with ID '" + id + "' restored.");
    }

/*    @Override
    public MessageResponse hardDeleteUserById(int id) {
        userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found!"));

        userRepository.deleteById(id);
        return new MessageResponse(true, HttpStatus.OK, "User with id  " + id + "' deleted!");
    }*/

    @Override
    public MessageResponse logout(User user) {
        if (user.getIsLoggedOut())
            throw new LoggedOutException("You are already logged out.");
        else {
            user.setIsLoggedOut(true);
            user.setJwtToken(null);
        }
        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "Logged out successful.");
    }

    @Override
    public MessageResponse changePassword(User loggedInUser, ChangePasswordDTO changePasswordDTO) {

        User user = userRepository.findByEmailIgnoreCase(loggedInUser.getUsername().trim());
        if (user == null)
            throw new ResourceNotFoundException("User with email '" + loggedInUser.getUsername() + "' not found!");

        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword()))
            throw new ResourceNotFoundException("Incorrect Old Password!");

        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword()))
            throw new ResourceNotFoundException("Old and New Password should not match!");

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setPwd(changePasswordDTO.getNewPassword());
        userRepository.save(user);
        return new MessageResponse(true, HttpStatus.OK, "Password Change Successfully!!");

    }

/*
    @Override
    public ListResponse exportedUserData() {
        List<UserExportDTO> users = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("email")));


            List<User> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            users = appUtils.userExportDTOStoUsers(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, users);
    }
*/

    @Override
    public MessageResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        User user = userRepository.findByEmailIgnoreCase(forgotPasswordDTO.getEmail());

        if (user == null)
            throw new ResourceNotFoundException("User with email '" + forgotPasswordDTO.getEmail() + "' not found!");

        Random random = new Random();
        String otp = String.valueOf(random.nextInt(900000) + 100000);

        emailService.sendForgotPasswordOtp(user.getEmail(), otp, user.getFullName());

        List<Confirmation> confirmations = confirmationRepository.findAllByEmailIgnoreCase(user.getEmail());
        if (!confirmations.isEmpty())
            confirmationRepository.deleteAll(confirmations);

        Confirmation confirmation = confirmationRepository.save(new Confirmation(user.getEmail(), otp));
        return new MessageResponse(true, HttpStatus.OK, "OTP to Change password has been sent to your email.");

    }

    @Override
    public MessageResponse verifyOtp(VerifyOtpDTO verifyOtpDTO) {
        Confirmation confirmation = confirmationRepository.findByOtp(verifyOtpDTO.getOtp());

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

        Confirmation conf = confirmationRepository.findByEmailIgnoreCase(verifyOtpDTO.getEmail());
        if (conf == null)
            throw new ResourceNotFoundException("Invalid Email");

        User user = userRepository.findByEmailIgnoreCase(confirmation.getEmail());
        if (user == null)
            throw new ResourceNotFoundException("User with email '" + confirmation.getEmail() + "' not found!");

        if (passwordEncoder.matches(verifyOtpDTO.getNewPassword(), user.getPassword()))
            throw new ResourceNotFoundException("Old and New password should not be same!");

        String newPassword = passwordEncoder.encode(verifyOtpDTO.getNewPassword());

        user.setPassword(newPassword);
        user.setPwd(verifyOtpDTO.getNewPassword());

        userRepository.save(user);

        confirmationRepository.delete(confirmation);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());

        return new MessageResponse(true, HttpStatus.OK, "OTP Verified and Your password changed successfully!!");

    }

    @Override
    public MessageResponse checkEmailExist(ForgotPasswordDTO forgotPasswordDTO) {
        User user = userRepository.findByEmailIgnoreCase(forgotPasswordDTO.getEmail().trim());
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        return new MessageResponse(true, HttpStatus.OK, "User found.");
    }

    @Override
    public MessageResponse validateToken(String token, User loggedInUser) {
        if (token != null && token.startsWith("Bearer ")) {
//            String authToken = token.substring(7);
            if (!jwtService.validateToken(token, loggedInUser)) {
                throw new TokenUnauthorizedException("Token is invalid");
            }
        }
        return new MessageResponse(true, HttpStatus.OK, "Token is valid");
    }


    @Override
    public DataResponse getUserByToken(User loggedInUser) {
        User user = userRepository.findByEmailIgnoreCase(loggedInUser.getEmail());

        return new DataResponse(true, HttpStatus.OK, appUtils.userToDto(userRepository.findById(user.getUserId()).
                orElseThrow(() -> new ResourceNotFoundException("User with id '" + user.getUserId() + "' not found!"))));
    }


    @Override
    public LongResponse getTotalUser() {
        Long count = userRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }



}
