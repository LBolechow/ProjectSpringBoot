package pl.lukbol.ProjectSpring.Services;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import pl.lukbol.ProjectSpring.Models.*;
import pl.lukbol.ProjectSpring.Repositories.*;
import pl.lukbol.ProjectSpring.Utils.ActionLogProducer;
import pl.lukbol.ProjectSpring.Utils.JwtUtil;
import pl.lukbol.ProjectSpring.Utils.MailProducer;
import pl.lukbol.ProjectSpring.Utils.UserUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    public enum ActionType {
        LOG("Logowanie"),
        REGISTER("Rejestracja"),
        CHANGE_PROFILE("Zmiana danych konta"),
        DELETE_USER("Użytkownik został usunięty"),
        RESET_MAIL("Wysłano e-mail z do restowania hasła");

        private final String action;

        ActionType(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    public enum userRole{
        ROLE_ADMIN,
        ROLE_CLIENT
    }

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserUtils userUtils;
    private final String urlPath = "http://localhost:8080/main";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordTokenRepository passwordTokenRepository;

    private final AuthenticationManager authenticationManager;

    private final ActivationTokenRepository activationTokenRepository;

    private final BlacklistedTokenRepository blacklistedTokenRepository;



    private final MailProducer mailProducer;

    private final ActionLogProducer actionLogProducer;




    public ResponseEntity<Map<String, Object>> authenticateUser(String usernameOrEmail,
                                                                String password) {
        String username;
        try {
            if (usernameOrEmail.contains("@") && usernameOrEmail.contains(".")) {
                User userByEmail = userRepository.findByEmail(usernameOrEmail);
                if (userByEmail == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("message", "Nie znaleziono użytkownika o takim adresie email."));
                }
                username = userByEmail.getUsername();
            } else {
                username = usernameOrEmail;
            }
            User user = userRepository.findOptionalByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Brak użytkownika z taką nazwą: " + username));
            if (!user.isActivated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Użytkownik nie jest aktywowany. Sprawdź swój adres email"));
            }


            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(username);
            Map<String, Object> response = userUtils.buildLoginResponse(token, username, urlPath);
            actionLogProducer.sendActionLog(username,ActionType.LOG.getAction() );
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Błędna nazwa użytkownika/email lub hasło."));
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> registerUser(String username, String name, String surname, String email, String phoneNumber, String password) {

        if (userUtils.emailExists(email)) {
            return userUtils.createErrorResponse("Użytkownik o takim adresie email już istnieje.");
        }

        if (userUtils.phoneNumberExists(phoneNumber)) {
            return userUtils.createErrorResponse("Użytkownik o takim numerze telefonu już istnieje.");
        }
        if (userUtils.usernameExists(username)) {
            return userUtils.createErrorResponse("Użytkownik o takiej nazwie użytkownika już istnieje.");
        }

        if (!userUtils.isValidPassword(password)) {
            return userUtils.createErrorResponse("Hasło musi spełniać określone kryteria bezpieczeństwa.");
        }


        User regUser = new User(name, surname, email, phoneNumber, passwordEncoder.encode(password), username, false);

        Role role = roleRepository.findByName(userRole.ROLE_CLIENT.name());
        regUser.setRoles(Arrays.asList(role));

        try {
            userRepository.save(regUser);
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        try {
            userUtils.createAccountActivationToken(regUser.getEmail());
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        actionLogProducer.sendActionLog(username,ActionType.REGISTER.getAction() );
        return userUtils.createSuccessResponse("Poprawnie utworzono konto. Na adres email został wysłany link aktywacyjny.");
    }

    @Transactional
    public ResponseEntity<User> getUserDetails(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();

        String username = ((UserDetails) principal).getUsername();

        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        return ResponseEntity.ok(user);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> changeProfile(Authentication authentication,
                                                             String name,
                                                             String surname,
                                                             String email,
                                                             String phoneNumber,
                                                             String password,
                                                             String repeatPassword) {

        Object principal = authentication.getPrincipal();
        String username = ((UserDetails) principal).getUsername();

        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (userUtils.isNullOrEmpty(name) || userUtils.isNullOrEmpty(surname) || userUtils.isNullOrEmpty(email) || userUtils.isNullOrEmpty(phoneNumber)) {
            return userUtils.createErrorResponse("Wszystkie wartości muszą być wypełnione.");
        }

        if (userUtils.isNullOrEmpty(password) && !userUtils.isNullOrEmpty(repeatPassword)) {
            return userUtils.createErrorResponse("Hasła są puste.");
        }

        if (!password.equals(repeatPassword)) {
            return userUtils.createErrorResponse("Hasła nie są takie same.");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return userUtils.createErrorResponse("Nowe hasło jest takie samo jak poprzednie.");
        }
        user.setPassword(passwordEncoder.encode(password));

        try {
            user.setName(name);
            user.setSurname(surname);
            user.setPhoneNumber(phoneNumber);
            user.setEmail(email);
            userRepository.save(user);
            actionLogProducer.sendActionLog(username,ActionType.CHANGE_PROFILE.getAction() );
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        return userUtils.createSuccessResponse("Poprawnie zapisano zmiany.");
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(Authentication authentication) {

        Object principal = authentication.getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika z nazwą: " + username));

        activationTokenRepository.deleteByUserId(user.getId());
        passwordTokenRepository.deleteByUserId(user.getId());

        try {
            userRepository.delete(user);
            actionLogProducer.sendActionLog(username,ActionType.DELETE_USER.getAction() );
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        return userUtils.createSuccessResponse("Poprawnie usunięto konto.");
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> sendResetPasswordEmail(String email) {
        User user = userRepository.findOptionalByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Brak użytkownika z emailem: " + email));

        passwordTokenRepository.deleteByUserId(user.getId());

        try {
            String token = userUtils.createPasswordResetTokenForUser(user);
            String to = user.getEmail();
            String subject = "Resetowanie hasła";
            String content = "Link do resetowania hasła: " + userUtils.generatePasswordResetLink(token);
            mailProducer.sendResetPasswordMail(to, subject, content);

        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }
        actionLogProducer.sendActionLog(user.getUsername(),ActionType.RESET_MAIL.getAction() );
        return userUtils.createSuccessResponse("Wysłano link do resetowania hasła na email.");
    }

    public ModelAndView showResetPasswordPage(String token) {

        if (userUtils.isNullOrEmpty(token)) {
            userUtils.createErrorResponse("Brak tokena w URL");
        }

        Optional<PasswordToken> passwordToken = passwordTokenRepository.findOptionalByToken(token);

        if (passwordToken.isEmpty() || passwordToken.get().isExpired()) {
            return new ModelAndView("error")
                    .addObject("message", "Token jest nieprawidłowy lub wygasł.");
        }

        ModelAndView modelAndView = new ModelAndView("reset");
        modelAndView.addObject("token", token);
        return modelAndView;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> resetPassword(String token, String password, String repeatPassword) {
        Optional<PasswordToken> passwordToken = passwordTokenRepository.findOptionalByToken(token);

        if (passwordToken.isEmpty() || passwordToken.get().isExpired()) {
            return userUtils.createErrorResponse("Token jest nieprawidłowy lub wygasł.");
        }

        String username = passwordToken.get().getUser().getUsername();
        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username + " powiązanego z tokenem"));


        if (userUtils.isNullOrEmpty(password) || userUtils.isNullOrEmpty(repeatPassword)) {
            return userUtils.createErrorResponse("Hasła są puste.");
        }


        if (!password.equals(repeatPassword)) {
            return userUtils.createErrorResponse("Hasła nie są takie same.");
        }


        if (passwordEncoder.matches(password, user.getPassword())) {
            return userUtils.createErrorResponse("Nowe hasło jest takie samo jak poprzednie.");
        }


        user.setPassword(passwordEncoder.encode(password));

        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        return userUtils.createSuccessResponse("Poprawnie zmieniono hasło.");
    }


    public ResponseEntity<Map<String, Object>> activateAccount(String token) {
        Optional<ActivationToken> optionalActivationToken = activationTokenRepository.findOptionalByToken(token);

        if (optionalActivationToken.isEmpty()) {
            return userUtils.createErrorResponse("Token jest nieprawidłowy lub wygasł.");
        }


        ActivationToken activationToken = optionalActivationToken.get();
        String username = activationToken.getUser().getUsername();

        if (userUtils.isNullOrEmpty(username)) {
            return userUtils.createErrorResponse("Nazwa użytkownika jest pusta.");
        }

        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username + " powiązanego z tokenem."));


        if (optionalActivationToken.get().isExpired()) {
            userUtils.createAccountActivationToken(user.getEmail());
            return userUtils.createErrorResponse("Token wygasł. Nowy zostanie wysłany ta ten sam adres email.");
        }

        try {
            user.setActivated(true);
            userRepository.save(user);
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }


        return userUtils.createSuccessResponse("Aktywowane konto! Możesz się zalogować");
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String token = jwtUtil.extractJwtFromRequest(request);

        if (token == null) {
            return userUtils.createErrorResponse("Nie znaleziono tokenu");
        }
        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(token);
        } catch (DataAccessException e) {
            return userUtils.createErrorResponse("Błąd: " + e.getMessage());
        }

        Date issuedAt = claims.getIssuedAt();
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, issuedAt);
        blacklistedTokenRepository.save(blacklistedToken);


        return userUtils.createSuccessResponse("Wylogowano pomyślnie");

    }




}
