package com.personaldiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.personaldiary.R;
import com.personaldiary.databinding.ActivitySignupBinding;
import com.personaldiary.utils.ValidationUtils;
import com.personaldiary.viewmodel.AuthViewModel;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupGoogleSignIn();
        setupObservers();
        setupListeners();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSignUp.setEnabled(!loading);
            binding.btnGoogleSignIn.setEnabled(!loading);
        });
    }

    private void setupListeners() {
        binding.btnSignUp.setOnClickListener(v -> attemptSignup());

        binding.btnGoogleSignIn.setOnClickListener(v -> launchGoogleSignIn());

        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void launchGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                viewModel.firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Snackbar.make(binding.getRoot(),
                        R.string.error_google_sign_in, Snackbar.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            Snackbar.make(binding.getRoot(),
                    R.string.error_google_sign_in, Snackbar.LENGTH_LONG).show();
        }
    }

    private void attemptSignup() {
        String name = getText(binding.etName);
        String email = getText(binding.etEmail);
        String password = getText(binding.etPassword);
        String confirmPassword = getText(binding.etConfirmPassword);

        clearErrors();

        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_empty_name));
            return;
        }
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            binding.tilPassword.setError(passwordError);
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_mismatch));
            return;
        }

        viewModel.signup(name, email, password);
    }

    private void clearErrors() {
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
