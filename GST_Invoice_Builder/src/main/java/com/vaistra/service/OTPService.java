package com.vaistra.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OTPService {

    @Autowired
    private SMSService smsService;

    private static final String OTP_CHARACTERS = "0123456789";
    private static final int OTP_LENGTH = 4;

    // Generate OTP
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(OTP_CHARACTERS.charAt(random.nextInt(OTP_CHARACTERS.length())));
        }

        return "4321";
//        return otp.toString();
    }

    // Send OTP via SMS (using Bulk SMS Gateway service)
    public void sendOtp(String phoneNumber, String otp) {
        smsService.sendOtp(phoneNumber, otp);
    }

    // OTP Verification
    public boolean verifyOtp(String generatedOtp, String enteredOtp) {
        return generatedOtp.equals(enteredOtp);
    }
}

