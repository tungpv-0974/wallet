package com.tungpv.wallet.config;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.io.IOException;

@Configuration
public class BitcoinConfig {
    @Value("${blockchain.bitcoin.network}")
    private String network;

    @Value("${blockchain.bitcoin.file-prefix}")
    private String filePrefix;

    @Value("${blockchain.bitcoin.file-location}")
    private String btcFileLocation;

    public BitcoinConfig() {
        BriefLogFormatter.init();
    }

    @Bean
    public NetworkParameters networkParameters() {
        switch (network) {
            case "testnet":
                return TestNet3Params.get();
            case "regtest":
                return RegTestParams.get();
            case "mainnet":
            default:
                return MainNetParams.get();
        }
    }

    @Bean
    public Context context(@Autowired NetworkParameters networkParameters) {
        return new Context(networkParameters);
    }

    @Bean
    public File locationsFile() {
        return new File(btcFileLocation, filePrefix + ".spvchain");
    }

    @Bean
    public WalletAppKit walletAppKit(@Autowired NetworkParameters networkParameters) {
        return new WalletAppKit(networkParameters, new File(btcFileLocation), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                if (wallet().getKeyChainGroupSize() < 1) {
                    wallet().importKey(new ECKey());
                }
            }
        };
    }

//    @Bean
//    @Scope("singleton")
//    public PeerGroup peerGroup(@Autowired NetworkParameters networkParameters, @Autowired File locationFile) throws BlockStoreException, IOException {
////        Context context = new Context(networkParameters);
////        Context.propagate(context);
//        SPVBlockStore spvBlockStore = new SPVBlockStore(networkParameters, locationFile);
//        BlockChain chain = new BlockChain(networkParameters, spvBlockStore);
//        PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
//        peerGroup.addPeerDiscovery(new DnsDiscovery(networkParameters));
//        return peerGroup;
//    }

}
