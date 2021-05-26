package com.tungpv.wallet.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Service
public class FirebaseInitializeService {

    @Value("${firebase.sdk-path-key}")
    private String credentialPath;

    @Value("${firebase.database-url}")
    private String databaseUrl;

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(credentialPath);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
