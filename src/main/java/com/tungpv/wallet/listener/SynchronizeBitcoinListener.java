package com.tungpv.wallet.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SynchronizeBitcoinListener {
//    @Autowired
//    private PeerGroup peerGroup;

    @Autowired
    private WalletAppKit walletAppKit;

    @Autowired
    private NetworkParameters networkParameters;

    @PostConstruct
    public void start() {

        walletAppKit.startAsync();
        walletAppKit.awaitRunning();

        walletAppKit.wallet().addCoinsReceivedEventListener(
                (wallet, tx, prevBalance, newBalance) -> {
                    Coin value = tx.getValueSentToMe(wallet);
                    System.out.println("Received tx for " + value.toFriendlyString());
                    Futures.addCallback(tx.getConfidence().getDepthFuture(1),
                            new FutureCallback<TransactionConfidence>() {
                                @Override
                                public void onSuccess(TransactionConfidence result) {
                                    System.out.println("Received tx " +
                                            value.toFriendlyString() + " is confirmed. ");
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                }
                            }, MoreExecutors.directExecutor());
                });

        Address sendToAddress = LegacyAddress.fromKey(networkParameters, walletAppKit.wallet().currentReceiveKey());
        System.out.println("Send coins to: " + sendToAddress);
        walletAppKit.stopAsync();

    }
}
