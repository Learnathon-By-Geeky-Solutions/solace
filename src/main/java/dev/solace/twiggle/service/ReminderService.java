package dev.solace.twiggle.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import dev.solace.twiggle.model.ReminderEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ReminderService {

    private final Resend resend;
    private final String fromEmail; // Configure this in your application properties
    private final String supabaseUrl;

    public ReminderService(
            @Value("${RESEND_API_KEY}") String apiKey, // Store your API key securely
            @Value("${RESEND_FROM_EMAIL}") String fromEmail,
            @Value("${SUPABASE_API_URL:https://ffihsfsyumfbvvrujxba.supabase.co}") String supabaseUrl) {
        // It's better practice to handle potential null/empty apiKey here
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_RESEND_API_KEY_HERE")) {
            log.warn("Resend API Key is not configured. Email sending will fail.");
            // Assign a dummy value or handle accordingly, maybe throw an exception
            // depending on whether email sending is critical at startup.
            this.resend = null; // Or initialize with a dummy key if Resend client allows
        } else {
            this.resend = new Resend(apiKey);
        }
        this.fromEmail = fromEmail;
        this.supabaseUrl = supabaseUrl;
    }

    /**
     * Sends a reminder email and returns a boolean indicating success
     */
    public boolean sendReminderEmail(ReminderEmailRequest request) {
        try {
            Map<String, Object> result = sendReminderEmailWithId(request);
            return (boolean) result.get("success");
        } catch (Exception e) {
            log.error("Error in sendReminderEmail: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Sends a reminder email and returns a map with success status and email ID if successful
     */
    public Map<String, Object> sendReminderEmailWithId(ReminderEmailRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        if (this.resend == null) {
            log.error("Resend client is not initialized. Cannot send email for plant: {}", request.getPlantName());
            logEmailDetails(request);
            return result;
        }

        String subject = String.format("🌿 Plant Care Reminder: %s for %s", request.getReminderType(), request.getPlantName());
        String htmlBody = generateHtmlEmail(request);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(this.fromEmail) // Use the configured 'from' email
                .to(request.getUserEmail())
                .subject(subject)
                .html(htmlBody)
                .build();

        try {
            log.info("Attempting to send reminder email to {} for plant {}", request.getUserEmail(), request.getPlantName());
            CreateEmailResponse data = resend.emails().send(params);
            String emailId = data.getId();
            log.info("Reminder email sent successfully. ID: {}", emailId);
            
            result.put("success", true);
            result.put("id", emailId);
            return result;
        } catch (ResendException e) {
            log.error("Error sending reminder email for plant {}: {}", request.getPlantName(), e.getMessage(), e);
            return result;
        } catch (Exception e) {
            // Catch other potential exceptions during setup or sending
            log.error("An unexpected error occurred while sending reminder email for plant {}: {}", request.getPlantName(), e.getMessage(), e);
            return result;
        }
    }

    private void logEmailDetails(ReminderEmailRequest request) {
        log.info("Intended subject: 🌿 Plant Care Reminder: {} for {}", request.getReminderType(), request.getPlantName());
        log.info("Garden Space: {}", request.getGardenSpaceName());
        log.info("Intended time: {} at {}", formatDate(request.getReminderDate()), request.getReminderTime());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            log.info("Image URL included: {}", request.getImageUrl());
        }
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            log.info("Notes: {}", request.getNotes());
        }
    }

    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US));
        } catch (Exception e) {
            log.warn("Error formatting date: " + dateStr, e);
            return dateStr; // Return original string if parsing fails
        }
    }

    private String generateHtmlEmail(ReminderEmailRequest request) {
        String formattedDate = formatDate(request.getReminderDate());
        
        // Build the HTML email template
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "      body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; }\n" +
                "      .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5; }\n" +
                "      .header { background-color: #2D3648; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }\n" +
                "      .content { background-color: #ffffff; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
                "      .plant-image-container { width: 100%; height: 300px; overflow: hidden; border-radius: 8px; margin: 20px 0; text-align: center; background-color: #f8f9fa; }\n" +
                "      .plant-image { width: 100%; height: 100%; object-fit: contain; }\n" +
                "      .reminder-box { background-color: #F3F4F6; padding: 20px; border-radius: 8px; margin: 20px 0; }\n" +
                "      .button { display: inline-block; background-color: #4F46E5; color: white; padding: 12px 24px; border-radius: 6px; text-decoration: none; margin-top: 20px; }\n" +
                "      .footer { text-align: center; margin-top: 20px; color: #6B7280; font-size: 14px; }\n" +
                "      .icon { font-size: 24px; margin-right: 8px; }\n" +
                "      .highlight { color: #4F46E5; font-weight: 600; }\n" +
                "      @media (max-width: 600px) {\n" +
                "        .container { padding: 10px; }\n" +
                "        .content { padding: 20px; }\n" +
                "      }\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <div class=\"container\">\n" +
                "      <div class=\"header\">\n" +
                "        <h1>Time for Plant Care! 🌱</h1>\n" +
                "      </div>\n" +
                "      <div class=\"content\">\n" +
                (hasValue(request.getImageUrl()) ? 
                "        <div class=\"plant-image-container\">\n" +
                "          <img src=\"" + request.getImageUrl() + "\" alt=\"" + request.getPlantName() + "\" class=\"plant-image\"/>\n" +
                "        </div>\n" : "") +
                "        \n" +
                "        <div class=\"reminder-box\">\n" +
                "          <h2>Reminder Details</h2>\n" +
                "          <p><strong>Plant:</strong> " + request.getPlantName() + "</p>\n" +
                "          <p><strong>Action:</strong> " + request.getReminderType() + "</p>\n" +
                "          <p><strong>When:</strong> " + formattedDate + " at " + request.getReminderTime() + "</p>\n" +
                "          <p><strong>Garden Space:</strong> " + request.getGardenSpaceName() + "</p>\n" +
                (hasValue(request.getNotes()) ? "          <p><strong>Notes:</strong> " + request.getNotes() + "</p>\n" : "") +
                "        </div>\n" +
                "\n" +
                "        <p>Your plant is waiting for some care and attention! Don't forget to " + request.getReminderType().toLowerCase() + " your " + request.getPlantName() + " at the scheduled time.</p>\n" +
                "\n" +
                "        <a href=\"" + supabaseUrl + "/garden-spaces/" + request.getGardenSpaceId() + "\" class=\"button\">\n" +
                "          View in Garden Space\n" +
                "        </a>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "          <p>Happy Gardening! 🌿</p>\n" +
                "          <p>This is an automated reminder from your Urban Garden Dashboard</p>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>";
    }
    
    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }
} 