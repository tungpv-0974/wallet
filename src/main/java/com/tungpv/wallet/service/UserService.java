package com.tungpv.wallet.service;

import com.tungpv.wallet.dto.request.SignUpDto;
import com.tungpv.wallet.dto.request.VerifyEmailDto;
import com.tungpv.wallet.entity.Role;
import com.tungpv.wallet.entity.User;
import com.tungpv.wallet.entity.VerificationToken;
import com.tungpv.wallet.entity.enums.RoleName;
import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.mail.OnRegistrationCompleteEvent;
import com.tungpv.wallet.repository.RoleRepository;
import com.tungpv.wallet.repository.UserRepository;
import com.tungpv.wallet.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService extends BaseService {
    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenVerifyService tokenVerifyService;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Transactional
    public void createUser(SignUpDto signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Creating user's account
        User user = new User(signUpRequest.getEmail(),
                signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()), false);

        Set<String> strRoles = Set.of("ROLE_USER");
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new BadRequestException("Cause: User Role not find."));
                    roles.add(adminRole);

                    break;
                case "pm":
                    Role pmRole = roleRepository.findByName(RoleName.ROLE_PM)
                            .orElseThrow(() -> new BadRequestException("Cause: User Role not find."));
                    roles.add(pmRole);

                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new BadRequestException("Cause: User Role not find."));
                    roles.add(userRole);
            }
        });

        user.setRoles(roles);
        userRepository.save(user);
        Locale locale = new Locale("vi");
        String appUrl = "http://localhost:8080";
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, locale, appUrl));
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    public void verifyEmail(VerifyEmailDto verifyEmailDto) {

        Locale locale = new Locale("vi");

        VerificationToken verificationToken = tokenVerifyService.getVerificationToken(verifyEmailDto.getToken());
        if (verificationToken == null) {
            throw new BadRequestException("Token invalid");
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new BadRequestException("Token expires");
        }

        user.setEnabled(true);
        userRepository.save(user);
        tokenVerifyService.deleteToken(verificationToken);
    }
}
