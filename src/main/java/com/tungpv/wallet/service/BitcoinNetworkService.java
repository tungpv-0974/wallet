package com.tungpv.wallet.service;


import com.tungpv.wallet.exception.BadRequestException;
import com.tungpv.wallet.exception.ServiceException;
import lombok.SneakyThrows;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component
public class BitcoinNetworkService {

    @Autowired
    private NetworkParameters networkParameters;

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

//    @Autowired
//    private PeerGroup peerGroup;

    @Autowired
    private File locationFile;

    public Wallet createWallet(String email) throws BlockStoreException {
        if (getWalletByUser(email) != null) {
            throw new BadRequestException("Wallet already exists");
        }
        String base64NameFolder = Base64.getEncoder().encodeToString(email.getBytes());
        String localWalletPath = walletDirectory.concat("/").concat(base64NameFolder);
        File file = new File(localWalletPath);
        Wallet wallet = Wallet.createDeterministic(networkParameters, Script.ScriptType.P2PKH);
        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @SneakyThrows
            @Override
            public synchronized void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAA");
                System.out.println("\nReceived tx  " + tx.getHashAsString());
                System.out.println(tx.toString());
                wallet.saveToFile(file);
            }
        });
        wallet.addReorganizeEventListener(new WalletReorganizeEventListener() {
            @SneakyThrows
            @Override
            public synchronized void onReorganize(Wallet wallet) {
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAA");
                wallet.saveToFile(file);
            }
        });
        wallet.addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
            @SneakyThrows
            @Override
            public synchronized void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                wallet.maybeCommitTx(tx);
                wallet.saveToFile(file);

            }
        });
//        wallet.add
        wallet.addChangeEventListener(new WalletChangeEventListener() {
            @SneakyThrows
            @Override
            public synchronized void onWalletChanged(Wallet wallet) {
                wallet.saveToFile(file);
            }
        });
//        peerGroup.addWallet(wallet);
//        peerGroup.startAsync();
//        peerGroup.downloadBlockChain();
//        peerGroup.stopAsync();

        SPVBlockStore spvBlockStore = new SPVBlockStore(networkParameters, locationFile);
        BlockChain chain = new BlockChain(networkParameters, spvBlockStore);
        PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(networkParameters));
        peerGroup.addWallet(wallet);
        peerGroup.startAsync();
        peerGroup.downloadBlockChain();
//        peerGroup.stopAsync();
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

