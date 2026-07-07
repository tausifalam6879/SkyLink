package com.skylink.service;

import com.skylink.entity.OtpPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendOtpEmail(
            String recipientEmail,
            String otpCode,
            OtpPurpose otpPurpose
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);

        if (otpPurpose == OtpPurpose.PASSWORD_RESET) {

            message.setSubject(
                    "SkyLink Password Reset OTP"
            );

            message.setText(
                    "SkyLink Password Reset\n\n"
                            + "Your password reset OTP is: "
                            + otpCode
                            + "\n\n"
                            + "This OTP is valid for 5 minutes.\n\n"
                            + "If you did not request a password reset, "
                            + "please ignore this email.\n\n"
                            + "Do not share this OTP with anyone.\n\n"
                            + "SkyLink Security Team"
            );

        } else if (otpPurpose == OtpPurpose.LOGIN) {

            message.setSubject(
                    "SkyLink Login OTP"
            );

            message.setText(
                    "SkyLink Secure Login\n\n"
                            + "Your login OTP is: "
                            + otpCode
                            + "\n\n"
                            + "This OTP is valid for 5 minutes.\n\n"
                            + "Do not share this OTP with anyone.\n\n"
                            + "SkyLink Security Team"
            );

        } else {

            message.setSubject(
                    "SkyLink Email Verification OTP"
            );

            message.setText(
                    "Welcome to SkyLink!\n\n"
                            + "Your email verification OTP is: "
                            + otpCode
                            + "\n\n"
                            + "This OTP is valid for 5 minutes.\n\n"
                            + "Do not share this OTP with anyone.\n\n"
                            + "SkyLink Security Team"
            );
        }

        javaMailSender.send(message);
    }
}
