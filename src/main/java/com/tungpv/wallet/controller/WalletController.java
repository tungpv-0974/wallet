package com.tungpv.wallet.controller;

import com.tungpv.wallet.dto.request.SendBitcoinDto;
import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.WalletBalanceDto;
import com.tungpv.wallet.dto.response.WalletCurrentReceiveAddressDto;
import com.tungpv.wallet.security.services.UserPrinciple;
import com.tungpv.wallet.service.BitcoinNetworkService;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/wallet")
@Valid
public class WalletController {

    @Autowired
    private BitcoinNetworkService bitcoinService;

    @PostMapping("/bitcoin/create")
    public ResponseEntity<CreateWalletResponseDto> createBitcoinWallet(@AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        return ResponseEntity.ok(bitcoinService.createWallet(userPrinciple.getEmail()));
    }

    @GetMapping("/bitcoin/get-address")
    public ResponseEntity<WalletCurrentReceiveAddressDto> getBitcoinAddress(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        return ResponseEntity.ok(bitcoinService.getCurrentReceiveAddress(userPrinciple.getEmail()));
    }

    @GetMapping("/bitcoin/get-balance")
    public ResponseEntity<WalletBalanceDto> getWalletBalance(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        Wallet wallet = bitcoinService.getWalletByUser(userPrinciple.getEmail());
        WalletBalanceDto responseDto = new WalletBalanceDto(wallet.getBalance().toFriendlyString());
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("bitcoin/send")
    public ResponseEntity sendCoinToAddress(@AuthenticationPrincipal UserPrinciple userPrinciple, @RequestBody SendBitcoinDto sendBitcoinDto) {
        bitcoinService.sendBitcoinToAddress(sendBitcoinDto, userPrinciple.getEmail());
        return ResponseEntity.noContent().build();
    }
}
