package dev.sara.micos_color_code.util;

public interface EmailService {
    void sendConfirmationEmail(String to, String username, String confirmationToken);
}