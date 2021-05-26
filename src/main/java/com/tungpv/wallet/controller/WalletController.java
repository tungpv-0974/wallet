package com.tungpv.wallet.controller;

import com.tungpv.wallet.dto.request.SendBitcoinDto;
import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.TransactionResponseDto;
import com.tungpv.wallet.dto.response.WalletBalanceDto;
import com.tungpv.wallet.dto.response.WalletCurrentReceiveAddressDto;
import com.tungpv.wallet.security.services.UserPrinciple;
import com.tungpv.wallet.service.BitcoinNetworkService;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/wallet")
@Valid
public class WalletController {

    @Autowired
    private BitcoinNetworkService bitcoinService;

    @PostMapping("/bitcoin/create")
    public ResponseEntity<CreateWalletResponseDto> createBitcoinWallet(@AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException, ExecutionException, InterruptedException {
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

    @GetMapping("bitcoin/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransaction(@AuthenticationPrincipal UserPrinciple user) {
        List<TransactionResponseDto> transactions;
        Set<Transaction> setTransaction;
        Wallet wallet = bitcoinService.getWalletByUser(user.getEmail());
        setTransaction = wallet.getTransactions(true);
        transactions = setTransaction.stream().map(tx -> {
            TransactionResponseDto transactionResponseDto = new TransactionResponseDto();
            if (tx.getValue(wallet).isNegative()) {
                transactionResponseDto.setFee(tx.getFee() != null ? tx.getFee().toFriendlyString() : "UNKNOWN");
                if (tx.getConfidence().getDepthInBlocks() == 0) {
                    transactionResponseDto.setType("Sending");
                } else {
                    transactionResponseDto.setType("Sent");
                }
            } else {
                if (tx.getConfidence().getDepthInBlocks() == 0) {
                    transactionResponseDto.setType("Receiving");
                } else {
                    transactionResponseDto.setType("Received");
                }
            }
            transactionResponseDto.setHash(tx.getTxId().toString());
            transactionResponseDto.setValue(tx.getValue(wallet).toFriendlyString());
            transactionResponseDto.setConfirmations(tx.getConfidence().getDepthInBlocks());
            transactionResponseDto.setUpdateTime(tx.getUpdateTime().toString());
            return transactionResponseDto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
}
