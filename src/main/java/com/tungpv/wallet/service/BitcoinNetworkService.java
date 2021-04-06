package com.tungpv.wallet.service;


import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.exception.ServiceException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Component
public class BitcoinNetworkService {

    @Autowired
    private NetworkParameters networkParameters;

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

    public Wallet createWallet(String email) {
        if (getWalletByUser(email) != null) {
            throw new BadRequestException("Wallet already exists");
        }
        String base64NameFolder = Base64.getEncoder().encodeToString(email.getBytes());
        String localWalletPath = walletDirectory.concat("/").concat(base64NameFolder);
        File file = new File(localWalletPath);
        Wallet wallet = Wallet.createDeterministic(networkParameters, Script.ScriptType.P2PKH);
        try {
            wallet.saveToFile(file);
        } catch (IOException e) {
            throw new ServiceException("Server error");
        }
        return wallet;
    }

    public Wallet getWalletByUser(String email) {
        String filename = Base64.getEncoder().encodeToString(email.getBytes());
        String localWalletPath = walletDirectory.concat("/").concat(filename);
        File walletFile = new File(localWalletPath);
        if (!walletFile.exists() || walletFile.isDirectory()) {
            return null;
        }
        try {
            return Wallet.loadFromFile(walletFile, null);
        } catch (UnreadableWalletException e) {
            throw new ServiceException("Server error");
        }
    }
}
