package com.tungpv.wallet.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.tungpv.wallet.dto.response.CreateWalletResponseDto;
import com.tungpv.wallet.dto.response.TransactionResponseDto;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@DependsOn("firebaseInitializeService")
public class FirebaseService extends BaseService {

    private final Firestore dbFirestore = FirestoreClient.getFirestore();

    public String saveWallet(String walletId, CreateWalletResponseDto wallet) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("wallets").document(walletId).set(wallet);
        return collectionApiFuture.get().getUpdateTime().toString();
    }

    public void saveTransactions(String walletId, List<TransactionResponseDto> transactions) throws ExecutionException, InterruptedException {
        transactions.forEach(tr -> {
            ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("wallets").document(walletId).collection("transactions").document(tr.getHash()).set(tr);
        });
//        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("transactions").document(walletId).set(transactions);
    }

}
