package com.vaistra.service;

import com.vaistra.dto.*;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.UserApproveDTO;
import com.vaistra.dto.update.UserUpdateDTO;
import com.vaistra.entity.User;

public interface UserService {
    UserRegisterResponse addUser(UserDTO userDto);

    DataResponse getUserById(int id);

    HttpResponse getAllUsers(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String activeStatus, String isDeleted);

    MessageResponse updateUser(UserUpdateDTO userDto, int id, User loggedInUser);

    MessageResponse softDeleteUser(int id, User loggedInUser);

    MessageResponse restoreUser(int id, User loggedInUser);

    //    MessageResponse hardDeleteUserById(int id);
    MessageResponse logout(User loggedInUser);

    MessageResponse changePassword(User loggedInUser, ChangePasswordDTO changePasswordDTO);

    //    ListResponse exportedUserData();
    MessageResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    MessageResponse verifyOtp(VerifyOtpDTO verifyOtpDTO);

    MessageResponse checkEmailExist(ForgotPasswordDTO forgotPasswordDTO);

    MessageResponse validateToken(String token, User loogedInUser);


    DataResponse getUserByToken(User loggedInUser);


    LongResponse getTotalUser();

}

