package com.personaldiary.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.personaldiary.firebase.FirebaseConfig;
import com.personaldiary.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.auth = FirebaseConfig.getAuth();
        this.db = FirebaseConfig.getDb();
    }

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email.trim(), password);
    }

    public Task<AuthResult> signup(String email, String password) {
        return auth.createUserWithEmailAndPassword(email.trim(), password);
    }

    public Task<AuthResult> signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return auth.signInWithCredential(credential);
    }

    public Task<Void> saveUserData(String userId, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put(Constants.FIELD_NAME, name.trim());
        userData.put(Constants.FIELD_EMAIL, email.trim());
        userData.put(Constants.FIELD_CREATED_AT, Timestamp.now());

        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .set(userData);
    }

    public Task<DocumentSnapshot> getUserData(String userId) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get();
    }

    public Task<Void> resetPassword(String email) {
        return auth.sendPasswordResetEmail(email.trim());
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
