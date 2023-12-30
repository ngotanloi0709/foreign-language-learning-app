package com.ngtnl1.foreign_language_learning_app.service;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ngtnl1.foreign_language_learning_app.model.User;
import com.ngtnl1.foreign_language_learning_app.repository.UserRepository;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {
    FirebaseAuth firebaseAuth;
    UserRepository userRepository;
    AppStatusService appStatusService;
    Context context;

    @Inject
    public UserService(FirebaseAuth firebaseAuth, UserRepository userRepository, AppStatusService appStatusService, Context context) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.appStatusService = appStatusService;
        this.context = context;
    }

    public Task<AuthResult> logIn(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            getUserDataRaw().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);

                if (user == null) {
                    user = new User(email);
                }

                userRepository.update(user.getEmail(), user);
            });
        });
    }

    public Task<AuthResult> register(String email, String password, String name, String phone, String dateOfBirth) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(email, name, phone, dateOfBirth);

                        return userRepository.createUser(user)
                                .continueWithTask(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        return task;
                                    } else {
                                        throw Objects.requireNonNull(createTask.getException());
                                    }
                                });
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public Task<DocumentSnapshot> getUserDataRaw() {
        return userRepository.find(getUserEmail());
    }

    public String getUserEmail() {
        return firebaseAuth.getCurrentUser().getEmail();
    }

    public Task<Void> setUser(User user) {
        return userRepository.update(user.getEmail(), user);
    }

    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public String getFirebaseErrorMessage(Exception exception) {
        try {
            if (!appStatusService.isOnline()) {
                return "Không có kết nối internet.";
            }

            switch (Objects.requireNonNull(exception.getMessage())) {
                case "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]":
                    return "Sai tên email hoặc mật khẩu.";
                case "The email address is already in use by another account.":
                    return "Email đã được sử dụng để đăng ký.";
                default:
                    return "Có lỗi xảy ra. Vui lòng thử lại.";
            }
        } catch (Exception e) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }
    }

    public Task<Boolean> checkPassword(String oldPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            return user.reauthenticate(credential).continueWith(Task::isSuccessful);
        }

        return Tasks.forException(new NullPointerException("No current user"));
    }

    public Task<Void> changePassword(String newPassword) {
        // Change the password
        return firebaseAuth.getCurrentUser().updatePassword(newPassword);
    }
}
