package com.ngtnl1.foreign_language_learning_app.repository;

import com.google.android.gms.tasks.Task;
import com.ngtnl1.foreign_language_learning_app.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository extends BaseRepository<User> {
    @Inject
    public UserRepository() {
        super("users");
    }

    public Task<Void> createUser(User user) {
        return  db.collection(collectionName)
                .document(user.getEmail())
                .set(user);
    }
}
