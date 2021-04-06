package com.tungpv.wallet.listener;

import com.tungpv.wallet.exception.ServiceException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class SynchronizeBitcoinListener {

    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private File locationFile;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        try {
            SPVBlockStore spvBlockStore = new SPVBlockStore(networkParameters, locationFile);
            BlockChain chain = new BlockChain(networkParameters, spvBlockStore);
            PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(networkParameters));
            peerGroup.start();
            peerGroup.downloadBlockChain();

        } catch (BlockStoreException ex) {
            ex.printStackTrace();
            throw new ServiceException("Server error");
        }
    }
}
