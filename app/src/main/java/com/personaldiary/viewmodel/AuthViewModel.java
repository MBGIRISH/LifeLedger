package com.personaldiary.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.personaldiary.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public AuthViewModel() {
        repository = new AuthRepository();
        if (repository.isLoggedIn()) {
            userLiveData.setValue(repository.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        repository.login(email, password)
                .addOnSuccessListener(authResult -> {
                    loadingLiveData.setValue(false);
                    userLiveData.setValue(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void signup(String name, String email, String password) {
        loadingLiveData.setValue(true);
        repository.signup(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        repository.saveUserData(user.getUid(), name, email)
                                .addOnSuccessListener(unused -> {
                                    loadingLiveData.setValue(false);
                                    userLiveData.setValue(user);
                                })
                                .addOnFailureListener(e -> {
                                    loadingLiveData.setValue(false);
                                    userLiveData.setValue(user);
                                });
                    } else {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Account creation failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void firebaseAuthWithGoogle(String idToken) {
        loadingLiveData.setValue(true);
        repository.signInWithGoogle(idToken)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Google Sign-In failed. Please try again.");
                        return;
                    }

                    repository.getUserData(user.getUid())
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    loadingLiveData.setValue(false);
                                    userLiveData.setValue(user);
                                } else {
                                    String name = user.getDisplayName() != null
                                            ? user.getDisplayName() : "";
                                    String email = user.getEmail() != null
                                            ? user.getEmail() : "";
                                    repository.saveUserData(user.getUid(), name, email)
                                            .addOnCompleteListener(task -> {
                                                loadingLiveData.setValue(false);
                                                userLiveData.setValue(user);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                loadingLiveData.setValue(false);
                                userLiveData.setValue(user);
                            });
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void resetPassword(String email) {
        loadingLiveData.setValue(true);
        repository.resetPassword(email)
                .addOnSuccessListener(unused -> {
                    loadingLiveData.setValue(false);
                    successMessage.setValue("Password reset email sent. Check your inbox.");
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(e.getMessage());
                });
    }

    public void logout() {
        repository.logout();
        userLiveData.setValue(null);
    }

    public boolean isLoggedIn() {
        return repository.isLoggedIn();
    }
}
