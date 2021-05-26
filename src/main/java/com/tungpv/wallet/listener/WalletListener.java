package com.tungpv.wallet.listener;

import com.google.protobuf.ByteString;
import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.TransactionResponseDto;
import com.tungpv.wallet.service.FirebaseService;
import com.tungpv.wallet.utils.Const;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletListener implements WalletChangeEventListener,
        WalletCoinsSentEventListener, WalletReorganizeEventListener, WalletCoinsReceivedEventListener, TransactionConfidenceEventListener {

    private String walletDirectory;

    private FirebaseService firebaseService;

    private NetworkParameters networkParameters;

    @SneakyThrows
    @Override
    public void onWalletChanged(Wallet wallet) {
    }

    @SneakyThrows
    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
    }

    @SneakyThrows
    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        saveWallet(wallet);
    }

    @SneakyThrows
    @Override
    public void onReorganize(Wallet wallet) {
    }

    @SneakyThrows
    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        saveWallet(wallet);
    }

    private void saveWallet(Wallet wallet) throws IOException, ExecutionException, InterruptedException {
        ByteString fileNameAsByteString = wallet.getTag(Const.PATH_FILE_TAG);
        String fileName = fileNameAsByteString.toStringUtf8();
        String localWalletPath = walletDirectory.concat(Const.SLASH).concat(fileName);
        File localWalletFile = new File(localWalletPath);
        WalletFiles walletFiles = new WalletFiles(wallet, localWalletFile, 3 * 1000, TimeUnit.MILLISECONDS);
        walletFiles.saveNow();
        CreateWalletResponseDto walletResponse = createWalletForSaveFirebase(wallet);
        List<TransactionResponseDto> transactions = createTransactionsForSaveFirebase(wallet);

        firebaseService.saveWallet(fileName, walletResponse);
        firebaseService.saveTransactions(fileName, transactions);
    }

    private CreateWalletResponseDto createWalletForSaveFirebase(Wallet wallet) {
        CreateWalletResponseDto walletResponse = new CreateWalletResponseDto();
        Address sendToAddress = LegacyAddress.fromKey(networkParameters, wallet.currentReceiveKey());
        walletResponse.setAddress(sendToAddress.toString());
        walletResponse.setBalance(wallet.getBalance().toFriendlyString());
        walletResponse.setMnemonicCode("");
        return walletResponse;
    }

    private List<TransactionResponseDto> createTransactionsForSaveFirebase(Wallet wallet) {
        Set<Transaction> setTransaction = wallet.getTransactions(true);
        return setTransaction.stream().map(tx -> {
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
    }
}
