package com.tungpv.wallet.listener;

import com.google.protobuf.ByteString;
import com.tungpv.wallet.utils.Const;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletListener implements WalletChangeEventListener,
        WalletCoinsSentEventListener, WalletReorganizeEventListener, WalletCoinsReceivedEventListener, TransactionConfidenceEventListener {

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

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

    private void saveWallet(Wallet wallet) throws IOException {
        ByteString fileNameAsByteString = wallet.getTag(Const.PATH_FILE_TAG);
        String fileName = fileNameAsByteString.toStringUtf8();
        String localWalletPath = walletDirectory.concat(Const.SLASH).concat(fileName);
        File localWalletFile = new File(localWalletPath);
        WalletFiles walletFiles = new WalletFiles(wallet, localWalletFile, 3 * 1000, TimeUnit.MILLISECONDS);
        walletFiles.saveNow();
    }
}
