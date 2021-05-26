package com.tungpv.wallet.service;

import com.tungpv.wallet.listener.WalletListener;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class BlockChainService {
    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

    @Autowired
    private PeerGroup peerGroup;

    @Autowired
    private BlockChain blockChain;

    @Autowired
    private List<Wallet> wallets;

    @Autowired
    private File locationsFile;

    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private File checkpointFile;

    @Autowired
    private FirebaseService firebaseService;

    @PostConstruct
    public void createBlockChain() {

        addWalletListeners();

        boolean blockChainFileExists = locationsFile.exists();
        if (!blockChainFileExists) {
            wallets.forEach(Wallet::reset);
        }
        wallets.forEach(System.out::println);

        long earliestKeyCreationTime = getEarliestKeyCreationTime();

        try {
            blockStore.getChainHead(); // detect corruptions as early as possible

            if (!blockChainFileExists && earliestKeyCreationTime > 0) {
                try {
                    final InputStream checkpointsInputStream = new FileInputStream(checkpointFile);
                    CheckpointManager.checkpoint(networkParameters, checkpointsInputStream,
                            blockStore, earliestKeyCreationTime);
                } catch (final IOException x) {
                    x.printStackTrace();
                }
            }
        } catch (final BlockStoreException x) {
            locationsFile.delete();
            x.printStackTrace();
        }
        wallets.forEach(wallet -> blockChain.addWallet(wallet));
        startup(earliestKeyCreationTime);
    }

    private long getEarliestKeyCreationTime() {
        return wallets.stream().mapToLong(Wallet::getEarliestKeyCreationTime)
                .max().orElse(Utils.currentTimeSeconds());
    }

    private void startup(long earliestKeyCreationTime) {
        System.out.println("startup: ");
        wallets.forEach(wallet -> peerGroup.addWallet(wallet));
        peerGroup.setFastCatchupTimeSecs(earliestKeyCreationTime);

        peerGroup.startAsync();
        peerGroup.startBlockChainDownload(null);
        peerGroup.downloadBlockChain();
        wallets.forEach(System.out::println);
    }

    private void addWalletListeners() {
        WalletListener walletListener = new WalletListener(walletDirectory, firebaseService, networkParameters);
        wallets.forEach(wallet -> {
            wallet.addChangeEventListener(walletListener);
            wallet.addCoinsReceivedEventListener(walletListener);
            wallet.addCoinsSentEventListener(walletListener);
            wallet.addReorganizeEventListener(walletListener);
            wallet.addTransactionConfidenceEventListener(walletListener);
        });
    }
}
