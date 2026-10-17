package Project;

import javafx.application.Platform;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String SENDER_EMAIL = "nyeinchankoko2262007123@gmail.com";
    private static final String APP_PASSWORD = "ydit svqj ttdc fkox";

    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Creates a JavaMail Session configured with timeouts and STARTTLS on Port 587.
     */
    private static Session createSmtpSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Explicit timeouts (in milliseconds) prevent infinite hanging
        props.put("mail.smtp.connectiontimeout", "15000"); // 15 seconds TCP connection timeout
        props.put("mail.smtp.timeout", "15000");           // 15 seconds socket read timeout
        props.put("mail.smtp.writetimeout", "15000");      // 15 seconds socket write timeout

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String cleanPassword = APP_PASSWORD.replaceAll("\\s+", "");
                return new PasswordAuthentication(SENDER_EMAIL, cleanPassword);
            }
        });

        session.setDebug(false);
        return session;
    }

    /**
     * Parses single or multiple email addresses delimited by commas, semicolons, or spaces.
     */
    private static InternetAddress[] parseRecipients(String recipientEmailsStr) throws Exception {
        if (recipientEmailsStr == null || recipientEmailsStr.isBlank()) {
            throw new IllegalArgumentException("Recipient email address cannot be empty.");
        }
        String normalized = recipientEmailsStr.replaceAll("[;\\s]+", ",");
        return InternetAddress.parse(normalized, false);
    }

    // Sends a password reset verification code asynchronously
    public static void sendPasswordResetCodeAsync(String recipientEmail, String resetCode, EmailCallback callback) {
        Thread emailThread = new Thread(() -> {
            try {
                Session session = createSmtpSession();

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, "Sodexa Platform"));
                message.setRecipients(Message.RecipientType.TO, parseRecipients(recipientEmail));
                message.setSubject("Password Reset Verification Code - Sodexa Platform");

                String htmlContent =
                        "<div style='font-family: Arial, sans-serif; padding: 24px; background-color: #0F172A; color: #FFFFFF; border-radius: 12px;'>" +
                                "<h2 style='color: #38BDF8; margin-top: 0;'>🔒 Password Reset Request</h2>" +
                                "<p style='color: #94A3B8; font-size: 15px; line-height: 1.5;'>" +
                                "We received a request to reset your password for your Sodex Platform account." +
                                "</p>" +
                                "<div style='background-color: #1E293B; padding: 20px; border-radius: 10px; text-align: center; margin: 20px 0; border: 1px solid #334155;'>" +
                                "<p style='margin: 0; color: #94A3B8; font-size: 12px; font-weight: bold; letter-spacing: 1px;'>YOUR VERIFICATION CODE</p>" +
                                "<h1 style='color: #34D399; letter-spacing: 6px; font-size: 36px; margin: 10px 0;'>" + resetCode + "</h1>" +
                                "</div>" +
                                "<p style='color: #94A3B8; font-size: 13px; margin-bottom: 0;'>" +
                                "This code will expire in 10 minutes. If you didn't request this, please ignore this email." +
                                "</p>" +
                                "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);

                if (callback != null) {
                    Platform.runLater(callback::onSuccess);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    Platform.runLater(() -> callback.onFailure(e));
                }
            }
        });

        emailThread.setDaemon(true);
        emailThread.start();
    }
}