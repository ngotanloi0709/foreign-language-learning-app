package com.ngtnl1.foreign_language_learning_app.controller.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.controller.MainActivity;
import com.ngtnl1.foreign_language_learning_app.databinding.FragmentMainProfileManagementBinding;
import com.ngtnl1.foreign_language_learning_app.model.User;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileManagementFragment extends Fragment {
    @Inject
    UserService userService;
    @Inject
    StorageReference storageReference;
    @Inject
    AppStatusService appStatusService;
    public static final int REQUEST_CODE_GALLERY = 1;
    private FragmentMainProfileManagementBinding binding;
    private final Calendar dateCalendar = Calendar.getInstance();
    private User user;
    private final Bitmap imageBitmap;

    public ProfileManagementFragment(User user, Bitmap imageBitmap) {
        this.user = user;
        this.imageBitmap = imageBitmap;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainProfileManagementBinding.inflate(inflater, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPress();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setData();
        setOnClickListeners();
    }

    private void setData() {
        try {
            if (user == null) {
                userService.getUserDataRaw().addOnSuccessListener(documentSnapshot -> user = documentSnapshot.toObject(User.class));
            }

            if (user != null) {
                binding.editTextMainProfileManagementName.setText(user.getName());
                binding.editTextMainProfileManagementDateOfBirth.setText(user.getDateOfBirth());
                binding.editTextMainProfileManagementPhone.setText(user.getPhone());
            } else {
                binding.editTextMainProfileManagementName.setText("");
                binding.editTextMainProfileManagementDateOfBirth.setText("");
                binding.editTextMainProfileManagementPhone.setText("");
            }

            Glide.with(this).load(imageBitmap).into(binding.imageMainProfileManagementAvatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOnClickListeners() {
        binding.buttonMainProfileManagementCamera.setOnClickListener(view -> openCamera());
        binding.buttonMainProfileManagementSave.setOnClickListener(view -> saveProfile());
        binding.editTextMainProfileManagementDateOfBirth.setOnClickListener(v -> showDatePicker());
        binding.buttonMainProfileManagementChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showDatePicker() {
        try {
            int year = dateCalendar.get(Calendar.YEAR);
            int month = dateCalendar.get(Calendar.MONTH);
            int day = dateCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(), (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {

                dateCalendar.set(Calendar.YEAR, selectedYear);
                dateCalendar.set(Calendar.MONTH, selectedMonth);
                dateCalendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                binding.editTextMainProfileManagementDateOfBirth.setText(format.format(dateCalendar.getTime()));

            }, year, month, day);
            datePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            } else {
                startGallery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery();
            } else {
                Toast.makeText(requireContext(), "Bạn không cho phép thư viện", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && appStatusService.isOnline()) {
            Uri imageUri = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageBitmap != null) {
                saveImageToFile(imageBitmap);
                binding.imageMainProfileManagementAvatar.setImageBitmap(imageBitmap);
            }
        }
    }


    private void saveImageToFile(Bitmap bitmap) {
        File file = new File(getCurrentPhotoPath());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentPhotoPath() {
        return requireContext().getFilesDir() + File.separator + "profile_image.jpg";
    }

    private void saveProfile() {
        try {
            if (!appStatusService.isOnline()) {
                Toast.makeText(requireContext(), "Không có kết nối internet", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null) {
                userService.getUserDataRaw().addOnSuccessListener(documentSnapshot -> user = documentSnapshot.toObject(User.class));
            }

            if (user != null) {
                user.setName(binding.editTextMainProfileManagementName.getText().toString());
                user.setDateOfBirth(binding.editTextMainProfileManagementDateOfBirth.getText().toString());
                user.setPhone(binding.editTextMainProfileManagementPhone.getText().toString());
                userService.setUser(user);

                uploadImage();

                Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                onBackPress();
            } else {
                Toast.makeText(requireContext(), "Lỗi, xin hãy thử lại", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi, xin hãy thử lại", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (appStatusService.isOnline()) {
            File file = new File(getCurrentPhotoPath());
            Uri fileUri = Uri.fromFile(file);

            if (fileUri != null) {
                storageReference.child("images/" + userService.getUserEmail() + ".jpg").putFile(fileUri).addOnCompleteListener(task -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.getData();
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Không thể cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show());
            }

            file.delete();
        } else {
            Toast.makeText(requireContext(), "Không có kết nối internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Đổi mật khẩu");

            View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, (ViewGroup) getView(), false);
            EditText oldPassword = viewInflated.findViewById(R.id.editTextDialogChangePasswordOldPassword);
            EditText newPassword = viewInflated.findViewById(R.id.editTextDialogChangePasswordNewPassword);
            EditText repeatPassword = viewInflated.findViewById(R.id.editTextDialogChangePasswordRepeatPassword);

            builder.setView(viewInflated);
            builder.setPositiveButton("Đổi mật khẩu", null);
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String oldPasswordText = oldPassword.getText().toString();
                String newPasswordText = newPassword.getText().toString();
                String repeatPasswordText = repeatPassword.getText().toString();

                if (!appStatusService.isOnline()) {
                    Toast.makeText(requireContext(), "Không có kết nối internet", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oldPasswordText.isEmpty() || newPasswordText.isEmpty() || repeatPasswordText.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPasswordText.length() < 6) {
                    Toast.makeText(requireContext(), "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPasswordText.equals(repeatPasswordText)) {
                    Toast.makeText(requireContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                userService.checkPassword(oldPasswordText).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        userService.changePassword(newPasswordText).addOnCompleteListener(changePasswordTask -> {
                            if (changePasswordTask.isSuccessful()) {
                                Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(requireContext(), "Lỗi khi đổi mật khẩu: " + changePasswordTask.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBackPress() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, new HomepageFragment(null, null)).addToBackStack(null).commit();
    }
}
