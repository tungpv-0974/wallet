package com.tungpv.wallet.service;


import com.google.protobuf.ByteString;
import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.WalletCurrentReceiveAddressDto;
import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.exception.ServiceException;
import com.tungpv.wallet.listener.WalletListener;
import com.tungpv.wallet.utils.Const;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
public class BitcoinNetworkService {

    @Autowired
    private NetworkParameters networkParameters;

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

    @Autowired
    private PeerGroup peerGroup;

    @Autowired
    private List<Wallet> wallets;

    public CreateWalletResponseDto createWallet(String email) throws IOException {
        if (getWalletByUser(email) != null) {
            throw new BadRequestException("Wallet already exists");
        }
        String base64NameFolder = Base64.getEncoder().encodeToString(email.getBytes());
        String localWalletPath = walletDirectory.concat(Const.SLASH).concat(base64NameFolder);
        Wallet wallet = Wallet.createDeterministic(networkParameters, Script.ScriptType.P2PKH);
        wallet.setTag(Const.PATH_FILE_TAG, ByteString.copyFromUtf8(base64NameFolder));

        addListener(wallet);

        File localWalletFile = new File(localWalletPath);
        WalletFiles walletFiles = new WalletFiles(wallet, localWalletFile, 3 * 1000, TimeUnit.MILLISECONDS);
        walletFiles.saveNow();

        peerGroup.addWallet(wallet);
        wallets.add(wallet);

        Address address = wallet.currentReceiveAddress();
        CreateWalletResponseDto responseDto = new CreateWalletResponseDto();

        DeterministicSeed seed = wallet.getKeyChainSeed();
        String mnemonicCode = Utils.SPACE_JOINER.join(Objects.requireNonNull(seed.getMnemonicCode()));

        responseDto.setAddress(address.toString());
        responseDto.setBalance(wallet.getBalance().getValue());
        responseDto.setMnemonicCode(mnemonicCode);

        return responseDto;
    }

    public WalletCurrentReceiveAddressDto getCurrentReceiveAddress(String email) {
        Wallet wallet = getWalletByUser(email);
        Address sendToAddress = LegacyAddress.fromKey(networkParameters, wallet.currentReceiveKey());
        return new WalletCurrentReceiveAddressDto(sendToAddress.toString());
    }

    public Wallet getWalletByUser(String email) {
        String filename = Base64.getEncoder().encodeToString(email.getBytes());
        String localWalletPath = walletDirectory.concat(Const.SLASH).concat(filename);
        File walletFile = new File(localWalletPath);
        if (!walletFile.exists() || walletFile.isDirectory()) {
            throw new BadRequestException("User has not created a wallet!");
        }
        try {
            return Wallet.loadFromFile(walletFile, null);
        } catch (UnreadableWalletException e) {
            throw new ServiceException("Server error");
        }
    }

    private Wallet addListener(Wallet wallet) {
        WalletListener walletListener = new WalletListener(walletDirectory);
        wallet.addChangeEventListener(walletListener);
        wallet.addCoinsReceivedEventListener(walletListener);
        wallet.addCoinsSentEventListener(walletListener);
        wallet.addReorganizeEventListener(walletListener);
        wallet.addTransactionConfidenceEventListener(walletListener);
        return wallet;
    }
}

