package com.tungpv.wallet.controller;

import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.WalletBalanceDto;
import com.tungpv.wallet.security.services.UserPrinciple;
import com.tungpv.wallet.service.BitcoinNetworkService;
import org.bitcoinj.core.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/wallet")
@Valid
public class WalletController {

    @Autowired
    private BitcoinNetworkService bitcoinService;

//    @Autowired
//    private PeerGroup peerGroup;

    @Autowired
    private NetworkParameters networkParameters;

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

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

    @GetMapping("/bitcoin/get-address")
    public ResponseEntity<String> getBitcoinAddress(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        Wallet wallet = bitcoinService.getWalletByUser(userPrinciple.getEmail());
        Address sendToAddress = LegacyAddress.fromKey(networkParameters, wallet.currentReceiveKey());

        return ResponseEntity.ok(sendToAddress.toString());
    }

    @GetMapping("/bitcoin/get-balance")
    public ResponseEntity<WalletBalanceDto> getWalletBalance(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        Wallet wallet = bitcoinService.getWalletByUser(userPrinciple.getEmail());

//        peerGroup.addWallet(wallet);
//        peerGroup.startAsync();
        WalletBalanceDto responseDto = new WalletBalanceDto(wallet.getBalance().getValue());
//        peerGroup.stop();
        return ResponseEntity.ok(responseDto);
    }
}
