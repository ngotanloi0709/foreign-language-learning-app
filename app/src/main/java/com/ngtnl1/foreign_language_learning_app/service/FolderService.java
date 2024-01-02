package com.ngtnl1.foreign_language_learning_app.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.model.Folder;
import com.ngtnl1.foreign_language_learning_app.repository.FolderRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FolderService {
    FolderRepository folderRepository;

    @Inject
    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public Task<DocumentReference> create(Folder folder) {
        return folderRepository.create(folder);
    }

    public Task<QuerySnapshot> getFoldersByUserId(String userId) {
        return folderRepository.getFoldersByUserId(userId);
    }

    public Task<Void> update(String id, Folder folder) {
        return folderRepository.update(id, folder);
    }

    public Task<Void> remove(String id) {
        return folderRepository.remove(id);
    }

    public Task<DocumentSnapshot> getFolderById(String id) {
        return folderRepository.find(id);
    }
}
