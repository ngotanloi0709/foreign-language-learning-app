package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.databinding.ActivityLoginBinding;
import com.ngtnl1.foreign_language_learning_app.service.AuthValidationService;
import com.ngtnl1.foreign_language_learning_app.service.UserService;


import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    @Inject
    AuthValidationService authValidationService;
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setOnClickListener();
    }

    private void initViews() {
        // setup view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setOnClickListener() {
        binding.buttonLoginLogin.setOnClickListener(v -> login());
        binding.buttonLoginChangeToRegister.setOnClickListener(v -> changeToRegister());
        binding.buttonLoginChangeToResetPassword.setOnClickListener(v -> changeToResetPassword());
    }

    private void login() {
        try {
            String email = binding.textInputEditTextLoginEmail.getText().toString();
            String password = binding.textInputEditTextLoginPassword.getText().toString();

            if (isValidInput(email, password)) {
                userService.logIn(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                changeToMain();
                            }
                        })
                        .addOnFailureListener(e -> {
                            String errorMessage = userService.getFirebaseErrorMessage(e);
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Đã xảy ra lỗi!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidInput(String email, String password) {
        boolean isValid = true;

        if (password.isEmpty()) {
            binding.textInputEditTextLoginPassword.setError("Vui lòng nhập mật khẩu!");
            binding.textInputEditTextLoginPassword.requestFocus();
            isValid = false;
        } else if (!authValidationService.isPasswordValid(password)) {
            binding.textInputEditTextLoginPassword.setError("Mật khẩu cần ít nhất 6 ký tự!");
            binding.textInputEditTextLoginPassword.requestFocus();
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.textInputEditTextLoginEmail.setError("Vui lòng nhập email!");
            binding.textInputEditTextLoginEmail.requestFocus();
            isValid = false;
        } else if (!authValidationService.isEmailValid(email)) {
            binding.textInputEditTextLoginEmail.setError("Email không hợp lệ!");
            binding.textInputEditTextLoginEmail.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void changeToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void changeToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void changeToResetPassword() {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
    @Override
    public void onStart() {
        super.onStart();

        if (userService.isUserSignedIn()) {
            finish();
        }
    }
}