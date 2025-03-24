package login.api.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import login.api.config.TwilioConfig;
import login.api.model.User;
import login.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Qualifier("twilio")
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final TwilioConfig twilioConfig;
    private final UserService userService;


    @Autowired
    public AuthService(TwilioConfig twilioConfig, UserService userService){

        this.twilioConfig = twilioConfig;
        this.userService = userService;
        Twilio.init(twilioConfig.getAccountSid(),twilioConfig.getAuthToken());
        logger.info("Twilio Initialized");
    }

    public void sendOtp(String phoneNumber) {
        if(isPhoneNumberValid(phoneNumber)) {
            try {
                PhoneNumber toPhoneNumber = new PhoneNumber(phoneNumber);
                PhoneNumber sourcePhoneNumber = new PhoneNumber(twilioConfig.getTwilioPhoneNumber());

                String otp = String.valueOf(new Random().nextInt(900000) + 100000);
                Message message = Message.creator(
                        toPhoneNumber,
                        sourcePhoneNumber,
                        twilioConfig.getMessage().replace("otp",otp))
                        .create();

                userService.storeOtp(phoneNumber, otp);
                logger.info("OTP sent to {}: status: {}", phoneNumber, message.getStatus());
            } catch (ApiException e) {
                logger.error("Twilio error: {}", e.getMessage());
                throw new RuntimeException("Failed to send OTP");
            }
        } else {
            throw new IllegalArgumentException("Phone number '"+phoneNumber+"' is not a valid number");
        }

    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }


    public boolean verifyOtp(String phoneNumber, String otp) {
        return userService.verifyOtp(phoneNumber, otp);
    }

    }
