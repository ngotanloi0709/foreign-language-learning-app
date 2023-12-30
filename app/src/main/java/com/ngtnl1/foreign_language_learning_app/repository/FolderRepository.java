package com.ngtnl1.foreign_language_learning_app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.model.Folder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FolderRepository  extends BaseRepository<Folder> {
    @Inject
    public FolderRepository() {
        super("folders");
    }

    public Task<QuerySnapshot> getFoldersByUserId(String userId) {
        return collectionReference.whereEqualTo("userId", userId).get();
    }
}
