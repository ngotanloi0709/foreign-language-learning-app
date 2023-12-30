package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.databinding.ActivityForgotPasswordBinding;
import com.ngtnl1.foreign_language_learning_app.service.UserService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordActivity extends AppCompatActivity {
    @Inject
    FirebaseAuth firebaseAuth;
    @Inject
    UserService userService;
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setOnClickListener();
    }

    private void initViews() {
        // setup view binding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setOnClickListener() {
        binding.buttonForgotPasswordNext.setOnClickListener(v -> {
            String email = binding.textInputEditTextEmailForgotPassword.getText().toString().trim();

            if(!TextUtils.isEmpty((email))){
                ResetPassword(email);
            }else{
                binding.textInputEditTextEmailForgotPassword.setError("Email không đuợc để trống!");
            }
        });
    }

    private void ResetPassword(String strEmail){
        binding.buttonForgotPasswordNext.setVisibility(View.VISIBLE);
        firebaseAuth.sendPasswordResetEmail(strEmail).addOnSuccessListener(unused -> {
            Toast.makeText(ForgotPasswordActivity.this, "Đã gửi Email đặt lại mật khẩu!", Toast.LENGTH_SHORT).show();
            changeToLogin();
        }).addOnFailureListener(e -> {
            Toast.makeText(ForgotPasswordActivity.this, "Lỗi! Vui lòng thử lại." + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.buttonForgotPasswordNext.setVisibility(View.VISIBLE);
        });
    }

    private void changeToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(userService.isUserSignedIn()) {
            finish();
        }
    }
}