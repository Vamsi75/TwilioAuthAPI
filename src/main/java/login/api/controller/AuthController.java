package login.api.controller;

import login.api.model.User;
import login.api.service.AuthService;
import login.api.service.UserService;
import login.api.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService twilioOtpService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String phoneNumber) {
        logger.info("Received OTP request for: {}", phoneNumber);
        try {
            twilioOtpService.sendOtp(formatPhoneNumber(phoneNumber));
            return ResponseEntity.ok("OTP sent to " + phoneNumber);
        } catch (Exception e) {
            logger.error("OTP send failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("OTP send failed");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber, @RequestParam String otp) {
        try {
            phoneNumber = formatPhoneNumber(phoneNumber);

            boolean isValid = twilioOtpService.verifyOtp(phoneNumber, otp);
            if (isValid) {
                User user = userService.createOrUpdateUser(phoneNumber);
                UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
                final String token = jwtTokenUtil.generateToken(userDetails);
                logger.info("OTP verified with token: {}", token);
                return ResponseEntity.ok(new JwtResponse(token));
            } else {
                return ResponseEntity.badRequest().body("Invalid OTP!");
            }
        } catch (Exception e) {
            logger.error("OTP verify failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("OTP verification failed: " + e.getMessage());
        }
    }

    static class JwtResponse {
        private final String token;

        public JwtResponse(String token) {
            this.token = token;
        }
        public String getToken() {
            return token;
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        String formattedNumber = phoneNumber.trim();
        if (!formattedNumber.startsWith("+")) {
            formattedNumber = "+" + formattedNumber;
        }
        return formattedNumber;
    }
}
