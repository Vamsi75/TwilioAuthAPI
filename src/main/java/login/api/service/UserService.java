package login.api.service;

import jakarta.transaction.Transactional;
import login.api.model.User;
import login.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
@Service
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User storeOtp(String mobileNumber, String otp) {
        return userRepository.findByMobileNumber(mobileNumber)
                .map(user -> {
                    user.setLoginOtp(otp);
                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user);
                }).orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(mobileNumber);
                    newUser.setMobileNumber(mobileNumber);
                    newUser.setLoginOtp(otp);
                    newUser.setLastLogin(LocalDateTime.now());
                    newUser.setCreatedDate(LocalDateTime.now());
                    newUser.setActive(true);
                    return userRepository.save(newUser);
                });
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        return userRepository.findByMobileNumber(mobileNumber)
                .map(user -> {
                    if (otp.equals(user.getLoginOtp())) {

                        LocalDateTime otpTime = user.getLastLogin();
                        LocalDateTime now = LocalDateTime.now();
                        Duration duration = Duration.between(otpTime, now);

                        if (duration.toSeconds() <= 20000) {
                            user.setLoginOtp("");
                            user.setLastLogin(now);
                            userRepository.save(user);
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    public User createOrUpdateUser(String mobileNumber) {
        return userRepository.findByMobileNumber(mobileNumber)
                .map(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(mobileNumber);
                    newUser.setMobileNumber(mobileNumber);
                    newUser.setLastLogin(LocalDateTime.now());
                    newUser.setCreatedDate(LocalDateTime.now());
                    newUser.setActive(true);
                    return userRepository.save(newUser);
                });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByMobileNumber(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getMobileNumber(),
                        "",
                        new ArrayList<>()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
