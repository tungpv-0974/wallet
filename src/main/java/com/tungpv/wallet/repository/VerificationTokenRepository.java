package com.tungpv.wallet.repository;

import com.tungpv.wallet.entity.User;
import com.tungpv.wallet.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);
}
