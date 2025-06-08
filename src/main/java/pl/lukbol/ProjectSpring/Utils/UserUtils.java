package pl.lukbol.ProjectSpring.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.lukbol.ProjectSpring.Models.ActivationToken;
import pl.lukbol.ProjectSpring.Models.PasswordToken;
import pl.lukbol.ProjectSpring.Models.User;
import pl.lukbol.ProjectSpring.Repositories.ActivationTokenRepository;
import pl.lukbol.ProjectSpring.Repositories.PasswordTokenRepository;
import pl.lukbol.ProjectSpring.Repositories.UserRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserUtils {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
    private final UserRepository userRepository;

    private final String urlPath = "http://localhost:8080/main";

    private final String tokenPath = "http://localhost:8080/activate?token=";

    private final String resetPath = "http://localhost:8080/reset?token=";
    private final PasswordTokenRepository passwordTokenRepository;
    private final ActivationTokenRepository activationTokenRepository;

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber) != null;
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }


    public ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    public ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    public Map<String, Object> buildLoginResponse(String token, String username, String urlPath) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("redirectUrl", urlPath);
        response.put("username", username);
        return response;
    }


    public boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public String createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000); //+ 1 godzina
        PasswordToken myToken = new PasswordToken(token, user, expiryDate);
        passwordTokenRepository.save(myToken);
        return token;
    }

    public void createAccountActivationToken(String email) {
        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));
        String token = UUID.randomUUID().toString();
        Date expiryDate = new Date(System.currentTimeMillis() + 24 * 3600000); //+ 24h
        ActivationToken myToken = new ActivationToken(token, user, expiryDate);
        activationTokenRepository.save(myToken);
        sendAccountActivationEmail(email, token);
    }

    public void sendAccountActivationEmail(String email, String token) {
        String activationLink = tokenPath + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Link aktywacyjny konta");
        message.setText("Kliknij w link aby aktywować konto: " + activationLink);

    }


    public String generatePasswordResetLink(String resetToken) {
        String baseUrl = "http://localhost:8080/resetPassword";
        return baseUrl + "?token=" + resetToken;
    }

}
