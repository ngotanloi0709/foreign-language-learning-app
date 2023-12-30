package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.databinding.ActivityRegisterBinding;
import com.ngtnl1.foreign_language_learning_app.service.UserService;
import com.ngtnl1.foreign_language_learning_app.service.AuthValidationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    @Inject
    AuthValidationService authValidationService;
    private Calendar dateCalendar = Calendar.getInstance();
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setOnClickListener();
    }

    private void initViews() {
        // setup view binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setOnClickListener() {
        binding.buttonRegisterRegister.setOnClickListener(v -> register());
        binding.buttonRegisterChangeToLogin.setOnClickListener(v -> changeToLogin());
        binding.textInputEditTextRegisterDateOfBirth.setOnClickListener(v -> showDatePicker());
    }

    private void register() {
        try {
            String name = binding.textInputEditTextRegisterFullname.getText().toString().replaceAll("\\s+", " ").trim();
            String email = binding.textInputEditTextRegisterEmail.getText().toString();
            String password = binding.textInputEditTextRegisterPassword.getText().toString();
            String phone = binding.textInputEditTextRegisterPhone.getText().toString();
            String dateOfBirth = binding.textInputEditTextRegisterDateOfBirth.getText().toString();

            if (isValidInput(name, email, password, phone, dateOfBirth)) {
                userService.register(email, password, name, phone, dateOfBirth)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            String errorMessage = userService.getFirebaseErrorMessage(e);
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isValidInput(String fullname, String email, String password, String phone, String dateOfBirth) {
        boolean isValid = true;

        if (password.isEmpty()) {
            binding.textInputEditTextRegisterPassword.setError("Vui lòng nhập mật khẩu!");
            binding.textInputEditTextRegisterPassword.requestFocus();
            isValid = false;
        } if (password.length() < 6) {
            binding.textInputEditTextRegisterPassword.setError("Mật khẩu cần ít nhất 6 ký tự!");
            binding.textInputEditTextRegisterPassword.requestFocus();
            isValid = false;
        } else if (!authValidationService.isRepeatPasswordValid(password, binding.textInputEditTextRegisterRepeatPassword.getText().toString())) {
            binding.textInputEditTextRegisterRepeatPassword.setError("Mật khẩu nhập lại không khớp!");
            binding.textInputEditTextRegisterRepeatPassword.requestFocus();
            isValid = false;
        }

        if (phone.isEmpty()) {
            binding.textInputEditTextRegisterPhone.setError("Vui lòng nhập số điện thoại!");
            binding.textInputEditTextRegisterPhone.requestFocus();
            isValid = false;
        } else if (!authValidationService.isPhoneValid(phone)) {
            binding.textInputEditTextRegisterPhone.setError("Số điện thoại không hợp lệ!");
            binding.textInputEditTextRegisterPhone.requestFocus();
            isValid = false;
        }
        if (dateOfBirth.isEmpty()) {
            binding.textInputEditTextRegisterDateOfBirth.setError("Vui lòng nhập ngày sinh!");
            binding.textInputEditTextRegisterDateOfBirth.requestFocus();
            isValid = false;
        }
        if (email.isEmpty()) {
            binding.textInputEditTextRegisterEmail.setError("Vui lòng nhập email!");
            binding.textInputEditTextRegisterEmail.requestFocus();
            isValid = false;
        } else if (!authValidationService.isEmailValid(email)) {
            binding.textInputEditTextRegisterEmail.setError("Email không hợp lệ!");
            binding.textInputEditTextRegisterEmail.requestFocus();
            isValid = false;
        }
        if (fullname.isEmpty()) {
            binding.textInputEditTextRegisterFullname.setError("Vui lòng nhập tên người dùng!");
            binding.textInputEditTextRegisterFullname.requestFocus();
            isValid = false;
        } else if (!authValidationService.isUsernameValid(fullname)) {
            binding.textInputEditTextRegisterFullname.setError("Tên người dùng không hợp lệ!");
            binding.textInputEditTextRegisterFullname.requestFocus();
            isValid = false;
        }
        return isValid;
    }

    private void changeToLogin() {
        finish();
    }

    private void showDatePicker() {
        int year = dateCalendar.get(Calendar.YEAR);
        int month = dateCalendar.get(Calendar.MONTH);
        int day = dateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {

            dateCalendar.set(Calendar.YEAR, selectedYear);
            dateCalendar.set(Calendar.MONTH, selectedMonth);
            dateCalendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.textInputEditTextRegisterDateOfBirth.setText(format.format(dateCalendar.getTime()));

        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (userService.isUserSignedIn()) {
            finish();
        }
    }
}