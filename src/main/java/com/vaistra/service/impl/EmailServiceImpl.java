package com.vaistra.service.impl;

import com.vaistra.dto.ForgotPasswordDTO;
import com.vaistra.entity.User;
import com.vaistra.repository.ConfirmationRepository;
import com.vaistra.repository.UserRepository;
import com.vaistra.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final ConfirmationRepository confirmationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, TemplateEngine templateEngine, UserRepository userRepository, ConfirmationRepository confirmationRepository) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
        this.confirmationRepository = confirmationRepository;
    }

    @Override
    @Async
    public void sendRegistrationEmail(String name, String confirmationToken, String to) {

        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("confirmationToken",confirmationToken);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("REGISTRATION SUCCESSFUL");
            String htmlContent = templateEngine.process("register.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    @Async
    public void sendForgotPasswordOtp(String to, String otp, String name) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("OTP VERIFICATION");
            String htmlContent = templateEngine.process("otp.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String name) {
        try{
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            context.setVariable("name", name);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("PASSWORD CHANGED");
            String htmlContent = templateEngine.process("passwordChanged.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Async
    public void sendEmail(ForgotPasswordDTO forgotPasswordDTO) {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        Context context = new Context();
        context.setVariable("name", forgotPasswordDTO.getEmail());

        try {
            helper.setTo(forgotPasswordDTO.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Email Example");
            String htmlContent = templateEngine.process("temp1.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String generateVerificationToken(User user) {
        // Implement your logic to generate a unique verification token
        // (e.g., using UUID.randomUUID())
        return UUID.randomUUID().toString();
    }

    @Async
    public void sendEmail(SimpleMailMessage email) {
        emailSender.send(email);
    }

    @Override
    public void sendAccountVerifiedEmail(String name, String to) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            context.setVariable("name", name);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Account Verified");
            String htmlContent = templateEngine.process("accountVerified.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void sendVerifyEmailOtp(String email, String otp) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            context.setVariable("otp", otp);
            helper.setTo(email);
            helper.setFrom(fromEmail);
            helper.setSubject("EMAIL OTP VERIFICATION");
            String htmlContent = templateEngine.process("emailOtp.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void sendEmailVerfication(String email) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            Context context = new Context();
            helper.setTo(email);
            helper.setFrom(fromEmail);
            helper.setSubject("Email Verified");
            String htmlContent = templateEngine.process("emailVerified.html", context);
            helper.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

