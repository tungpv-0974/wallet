package com.tungpv.wallet.service;


import com.tungpv.wallet.entity.VerificationToken;
import com.tungpv.wallet.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenVerifyService extends BaseService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    public void deleteToken(VerificationToken verificationToken) {
        tokenRepository.delete(verificationToken);
    }
}
