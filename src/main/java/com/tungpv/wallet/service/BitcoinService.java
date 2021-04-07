package com.tungpv.wallet.service;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;

public class BitcoinService implements WalletCoinsReceivedEventListener,
        WalletCoinsSentEventListener, WalletChangeEventListener {
    @Override
    public void onWalletChanged(Wallet wallet) {

    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {

    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {

    }
}
