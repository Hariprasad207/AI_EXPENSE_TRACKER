package com.example.Expense_Tracker.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendTestMail(String to,String otp){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("donotreply.expensetracker@gmail.com");
        message.setTo(to);
        message.setSubject("Expense Tracker - Test mail");
        message.setText("""
                Hello,

            We received a request to reset your Expense Tracker password.

            Your verification code is:

            %s

            This code will expire in 5 minutes.

            If you did not request a password reset, you can safely ignore this email.

            Regards,
            Expense Tracker
            """.formatted(otp));
        mailSender.send(message);
    }
}
