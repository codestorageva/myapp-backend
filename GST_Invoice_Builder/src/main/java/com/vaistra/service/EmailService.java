package com.vaistra.service;

import org.springframework.mail.SimpleMailMessage;

public interface EmailService
{
    void sendRegistrationEmail(String name, String confirmationToken, String to);
    void sendForgotPasswordOtp(String to, String otp, String name);
    void sendPasswordChangedEmail(String to, String name);
    void sendEmail(SimpleMailMessage mailMessage);
    void sendAccountVerifiedEmail(String name, String to);

    void sendVerifyEmailOtp(String email, String otp);

    void sendEmailVerfication(String email);

    //    void sendEmail(SimpleMailMessage mailMessage);
}
