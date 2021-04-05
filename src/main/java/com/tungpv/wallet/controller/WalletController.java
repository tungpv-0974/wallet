package com.tungpv.wallet.controller;

import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.security.services.UserPrinciple;
import com.tungpv.wallet.service.BitcoinNetworkService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Utils;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/wallet")
@Valid
public class WalletController {

    @Autowired
    private BitcoinNetworkService bitcoinService;

    @PostMapping("/bitcoin/create")
    public ResponseEntity<CreateWalletResponseDto> createBitcoinWallet(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        Wallet wallet = bitcoinService.createWallet(userPrinciple.getEmail());
        Address address = wallet.currentReceiveAddress();
        CreateWalletResponseDto responseDto = new CreateWalletResponseDto();

        DeterministicSeed seed = wallet.getKeyChainSeed();
        String mnemonicCode = Utils.SPACE_JOINER.join(Objects.requireNonNull(seed.getMnemonicCode()));

        responseDto.setAddress(address.toString());
        responseDto.setBalance(wallet.getBalance().getValue());
        responseDto.setMnemonicCode(mnemonicCode);
        return ResponseEntity.ok(responseDto);
    }
}
