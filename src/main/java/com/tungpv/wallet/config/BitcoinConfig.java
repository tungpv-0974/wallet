package com.tungpv.wallet.config;

import com.tungpv.wallet.utils.Const;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.PeerDiscoveredEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.MultiplexingDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.net.discovery.PeerDiscoveryException;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class BitcoinConfig {
    @Value("${blockchain.bitcoin.network}")
    private String network;

    @Value("${blockchain.bitcoin.file-prefix}")
    private String filePrefix;

    @Value("${blockchain.bitcoin.file-location}")
    private String btcFileLocation;

    @Value("${blockchain.bitcoin.wallet-directory}")
    private String walletDirectory;

    @Value("${blockchain.bitcoin.check-point}")
    private String checkpointFilePath;

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

    @Bean
    public File checkpointFile() {
        return new File(checkpointFilePath);
    }

    @Bean
    public List<Wallet> wallets() throws UnreadableWalletException {
        List<Wallet> wallets = new ArrayList<>();
        final File walletFolder = new File(walletDirectory);
        for (final File fileEntry : Objects.requireNonNull(walletFolder.listFiles())) {
            if (fileEntry.isDirectory()) {
//                do nothing
            } else {
                String walletFilePath = fileEntry.getAbsolutePath();
                File walletFile = new File(walletFilePath);
                Wallet wallet = Wallet.loadFromFile(walletFile, null);
                wallets.add(wallet);
            }
        }
        return wallets;
    }

    @Bean
    @Scope(Const.SINGLETON)
    public BlockStore blockStore(@Autowired NetworkParameters networkParameters, File locationsFile) throws BlockStoreException {
        return new SPVBlockStore(networkParameters, locationsFile);
    }

    @Bean
    @Scope(Const.SINGLETON)
    public BlockChain blockChain(@Autowired NetworkParameters networkParameters, @Autowired BlockStore blockStore) throws BlockStoreException {
        return new BlockChain(networkParameters, blockStore);
    }

    @Bean
    @Scope(Const.SINGLETON)
    public PeerGroup peerGroup(@Autowired NetworkParameters networkParameters, @Autowired BlockChain blockChain) {
        PeerGroup peerGroup = new PeerGroup(networkParameters, blockChain);
        peerGroup.setDownloadTxDependencies(0);
        peerGroup.setMaxConnections(8);
        int connectTimeout = 15 * 1000;
        peerGroup.setConnectTimeoutMillis(connectTimeout);
        int discoveryTimeout = 10 * 1000;
        peerGroup.addConnectedEventListener(mPeerConnectedEventListener);
        peerGroup.addDisconnectedEventListener(mPeerDisconnectedEventListener);
        peerGroup.addDiscoveredEventListener(mPeerDiscoveredEventListener);
        peerGroup.setPeerDiscoveryTimeoutMillis(discoveryTimeout);
        peerGroup.addPeerDiscovery(new PeerDiscovery() {
            private final PeerDiscovery normalPeerDiscovery = MultiplexingDiscovery
                    .forServices(networkParameters, 0);

            @Override
            public InetSocketAddress[] getPeers(final long services, final long timeoutValue,
                                                final TimeUnit timeoutUnit) throws PeerDiscoveryException {
                return normalPeerDiscovery.getPeers(services, timeoutValue, timeoutUnit);
            }

            @Override
            public void shutdown() {
                normalPeerDiscovery.shutdown();
            }
        });
        return peerGroup;
    }

    private PeerConnectedEventListener mPeerConnectedEventListener = new PeerConnectedEventListener() {
        @Override
        public synchronized void onPeerConnected(Peer peer, int peerCount) {
            System.out.println("onPeerConnected: " + peer.toString());
        }
    };

    private PeerDisconnectedEventListener mPeerDisconnectedEventListener = new PeerDisconnectedEventListener() {
        @Override
        public synchronized void onPeerDisconnected(Peer peer, int peerCount) {
            System.out.println("onPeerDisconnected: " + peer.toString());
        }
    };

    private PeerDiscoveredEventListener mPeerDiscoveredEventListener = new PeerDiscoveredEventListener() {
        @Override
        public synchronized void onPeersDiscovered(Set<PeerAddress> peerAddresses) {
            if (peerAddresses != null) {
                System.out.println("onPeersDiscovered: " + peerAddresses.iterator().next().getHostname());
            }
        }
    };

}
